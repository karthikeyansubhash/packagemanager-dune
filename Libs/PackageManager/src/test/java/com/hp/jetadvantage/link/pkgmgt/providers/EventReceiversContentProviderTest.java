package com.hp.jetadvantage.link.pkgmgt.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.hp.jetadvantage.link.pkgmgt.PackageContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 31)
public class EventReceiversContentProviderTest {

    private static final String PACKAGE_ONE = "com.hp.workpath.sample.eventnotificationsample";
    private static final String PACKAGE_TWO = "com.hp.workpath.sample.second";
    private static final String PACKAGE_THREE = "com.hp.workpath.sample.third";

    private Context applicationContext;
    private EventReceiversContentProvider provider;

    @Before
    public void setUp() throws Exception {
        applicationContext = ApplicationProvider.getApplicationContext();
        resetDbHelperSingleton();
        applicationContext.deleteDatabase("ConnectorPackages.db");

        provider = new EventReceiversContentProvider();
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = PackageContract.EVENTRECEIVERS_AUTHORITY;
        providerInfo.exported = true;
        providerInfo.enabled = true;
        provider.attachInfo(applicationContext, providerInfo);
        clearTable();
    }

    @After
    public void tearDown() {
        clearTable();
    }

    @Test
    public void insertAndCompatibilityQuery_returnsMatchingPackageNames() {
        String packageOne = PACKAGE_ONE + ".compat." + System.nanoTime();
        String packageTwo = PACKAGE_TWO + ".compat." + System.nanoTime();
        insertRow(PackageContract.WorkpathEventAction.SIGN_IN, packageOne);
        insertRow(PackageContract.WorkpathEventAction.SIGN_OUT, packageTwo);

        try (Cursor cursor = provider.query(
                PackageContract.EVENTRECEIVERS_CONTENT_URI,
                null,
                null,
                new String[]{PackageContract.WorkpathEventAction.SIGN_IN},
                null)) {
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(packageOne,
                    cursor.getString(cursor.getColumnIndexOrThrow(PackageContract.EventReceiverEntry.PACKAGE_NAME)));
        }
    }

    @Test
    public void queryByTypePath_returnsOnlyRowsForRequestedEventType() {
        String packageOne = PACKAGE_ONE + ".path." + System.nanoTime();
        String packageTwo = PACKAGE_TWO + ".path." + System.nanoTime();
        String packageThree = PACKAGE_THREE + ".path." + System.nanoTime();
        insertRow(PackageContract.WorkpathEventAction.SIGN_IN, packageOne);
        insertRow(PackageContract.WorkpathEventAction.SIGN_IN, packageTwo);
        insertRow(PackageContract.WorkpathEventAction.SLEEP, packageThree);

        // JOLT-style query contract: use base URI and pass eventType in selectionArgs[0].
        try (Cursor cursor = provider.query(
                PackageContract.EVENTRECEIVERS_CONTENT_URI,
                PackageContract.ALL_PROJECTION_EVENT_RECEIVERS,
                null,
                new String[]{PackageContract.WorkpathEventAction.SIGN_IN},
                PackageContract.EventReceiverEntry.PACKAGE_NAME + " ASC")) {
            assertNotNull(cursor);
            assertEquals(2, cursor.getCount());
            Set<String> packages = new HashSet<>();
            while (cursor.moveToNext()) {
                packages.add(cursor.getString(cursor.getColumnIndexOrThrow(PackageContract.EventReceiverEntry.PACKAGE_NAME)));
            }
            assertEquals(new HashSet<>(Arrays.asList(packageOne, packageTwo)), packages);
        }
    }

    @Test
    public void insertWithSameCompositeKey_replacesExistingRowWithoutCreatingDuplicates() {
        String packageOne = PACKAGE_ONE + ".replace." + System.nanoTime();
        insertRow(PackageContract.WorkpathEventAction.CONFIG_CHANGED, packageOne);
        insertRow(PackageContract.WorkpathEventAction.CONFIG_CHANGED, packageOne);

        try (Cursor cursor = provider.query(
                PackageContract.EVENTRECEIVERS_CONTENT_URI,
                PackageContract.ALL_PROJECTION_EVENT_RECEIVERS,
                PackageContract.EventReceiverEntry.EVENT_TYPE + "=? AND " + PackageContract.EventReceiverEntry.PACKAGE_NAME + "=?",
                new String[]{PackageContract.WorkpathEventAction.CONFIG_CHANGED, packageOne},
                null)) {
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
        }
    }

    @Test
    public void delete_removesOnlyMatchingRows() {
        String packageOne = PACKAGE_ONE + ".delete." + System.nanoTime();
        String packageTwo = PACKAGE_TWO + ".delete." + System.nanoTime();
        insertRow(PackageContract.WorkpathEventAction.WAKE_UP, packageOne);
        insertRow(PackageContract.WorkpathEventAction.WAKE_UP, packageTwo);

        int deleted = provider.delete(
                PackageContract.EVENTRECEIVERS_CONTENT_URI,
                PackageContract.EventReceiverEntry.PACKAGE_NAME + "=?",
                new String[]{packageOne});
        assertEquals(1, deleted);

        try (Cursor cursor = provider.query(
                PackageContract.EVENTRECEIVERS_CONTENT_URI,
                PackageContract.ALL_PROJECTION_EVENT_RECEIVERS,
                null,
                null,
                null)) {
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(packageTwo,
                    cursor.getString(cursor.getColumnIndexOrThrow(PackageContract.EventReceiverEntry.PACKAGE_NAME)));
        }
    }

    @Test
    public void insert_withInvalidUriReturnsNull() {
        Uri invalidUri = Uri.withAppendedPath(PackageContract.EVENTRECEIVERS_CONTENT_URI,
                PackageContract.WorkpathEventAction.SIGN_IN);
        try {
            provider.insert(invalidUri, new ContentValues());
            org.junit.Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unknown or invalid URI"));
        }
    }

    private void clearTable() {
        provider.delete(PackageContract.EVENTRECEIVERS_CONTENT_URI, null, null);
    }

    private void insertRow(String eventType, String packageName) {
        ContentValues values = new ContentValues();
        values.put(PackageContract.EventReceiverEntry.EVENT_TYPE, eventType);
        values.put(PackageContract.EventReceiverEntry.PACKAGE_NAME, packageName);
        provider.insert(PackageContract.EVENTRECEIVERS_CONTENT_URI, values);

        try (Cursor cursor = provider.query(
                PackageContract.EVENTRECEIVERS_CONTENT_URI,
                PackageContract.ALL_PROJECTION_EVENT_RECEIVERS,
                PackageContract.EventReceiverEntry.EVENT_TYPE + "=? AND " + PackageContract.EventReceiverEntry.PACKAGE_NAME + "=?",
                new String[]{eventType, packageName},
                null)) {
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
        }
    }

    private void resetDbHelperSingleton() throws Exception {
        Field instanceField = PackageDBHelper.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
