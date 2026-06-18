package com.hp.jetadvantage.link.pkgmgt.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;

import androidx.test.core.app.ApplicationProvider;

import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.providers.EventReceiversContentProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowPackageManager;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 31)
public class EventReceiverManagerTest {

    private static final String TEST_PACKAGE = "com.hp.workpath.sample.eventnotificationsample";
    private static final String SECOND_PACKAGE = "com.hp.workpath.sample.second";

    private Context applicationContext;
    private PackageManager packageManager;
    private ShadowPackageManager shadowPackageManager;
    private EventReceiverManager eventReceiverManager;

    @Before
    public void setUp() throws Exception {
        applicationContext = ApplicationProvider.getApplicationContext();
        resetDbHelperSingleton();
        applicationContext.deleteDatabase("ConnectorPackages.db");
        packageManager = applicationContext.getPackageManager();
        shadowPackageManager = Shadow.extract(packageManager);
        registerProvider(applicationContext);
        clearTable();
        eventReceiverManager = new EventReceiverManager(new ContextWrapper(applicationContext) {
            @Override
            public PackageManager getPackageManager() {
                return packageManager;
            }
        });
    }

    @After
    public void tearDown() {
        clearTable();
    }

    @Test
    public void registerEventReceivers_filtersByPermissionAndAvoidsDuplicateActions() {
        String testPackage = TEST_PACKAGE + ".register";
        installPackage(testPackage);
        addReceiver(testPackage, PackageContract.WorkpathEventAction.SIGN_IN,
                createResolveInfo(PackageContract.WorkpathEventAction.SIGN_IN,
                        "com.hp.workpath.permission.RECEIVE_SIGN_IN_OUT_EVENT"));
        addReceiver(testPackage, PackageContract.WorkpathEventAction.SIGN_OUT,
                createResolveInfo(PackageContract.WorkpathEventAction.SIGN_OUT, "wrong.permission"));
        addReceiver(testPackage, PackageContract.WorkpathEventAction.JOB_COMPLETED,
                createResolveInfo(PackageContract.WorkpathEventAction.JOB_COMPLETED,
                        "com.hp.workpath.permission.RECEIVE_JOB_COMPLETED_EVENT"));

        boolean registered = eventReceiverManager.registerEventReceivers(testPackage);

        assertTrue(registered);
        assertRowCountFor(testPackage, PackageContract.WorkpathEventAction.SIGN_IN, 1);
        assertRowCountFor(testPackage, PackageContract.WorkpathEventAction.JOB_COMPLETED, 1);
        assertRowCountFor(testPackage, PackageContract.WorkpathEventAction.SIGN_OUT, 0);
    }

    @Test
    public void unregisterEventReceivers_deletesRowsForPackage() {
        String testPackage = TEST_PACKAGE + ".unregister";
        String secondPackage = SECOND_PACKAGE + ".unregister";
        installPackage(testPackage);
        installPackage(secondPackage);
        addReceiver(testPackage, PackageContract.WorkpathEventAction.SIGN_IN,
                createResolveInfo(PackageContract.WorkpathEventAction.SIGN_IN,
                        "com.hp.workpath.permission.RECEIVE_SIGN_IN_OUT_EVENT"));
        addReceiver(testPackage, PackageContract.WorkpathEventAction.SIGN_OUT,
                createResolveInfo(PackageContract.WorkpathEventAction.SIGN_OUT,
                        "com.hp.workpath.permission.RECEIVE_SIGN_IN_OUT_EVENT"));
        addReceiver(secondPackage, PackageContract.WorkpathEventAction.SLEEP,
                createResolveInfo(PackageContract.WorkpathEventAction.SLEEP,
                        "com.hp.workpath.permission.RECEIVE_SLEEP_WAKEUP_EVENT"));

        eventReceiverManager.registerEventReceivers(testPackage);
        eventReceiverManager.registerEventReceivers(secondPackage);

        boolean unregistered = eventReceiverManager.unregisterEventReceivers(testPackage);

        assertTrue(unregistered);
        assertRowCountFor(testPackage, PackageContract.WorkpathEventAction.SIGN_IN, 0);
        assertRowCountFor(testPackage, PackageContract.WorkpathEventAction.SIGN_OUT, 0);
        assertRowCountFor(secondPackage, PackageContract.WorkpathEventAction.SLEEP, 1);
    }

    @Test
    public void registerEventReceivers_returnsFalseForEmptyPackageName() {
        boolean registered = eventReceiverManager.registerEventReceivers("");

        assertFalse(registered);
        assertEquals(0, getRowCount());
    }

    @Test
    public void scanInstalledPackagesAndRebuildTable_clearsTableAndRegistersRecognizedPackages() {
        String testPackage = TEST_PACKAGE + ".scan";
        String secondPackage = SECOND_PACKAGE + ".scan";
        insertRow(PackageContract.WorkpathEventAction.SIGN_OUT, "stale.package");
        installPackage(testPackage);
        installPackage(secondPackage);
        addReceiver(testPackage, PackageContract.WorkpathEventAction.CONFIG_CHANGED,
                createResolveInfo(PackageContract.WorkpathEventAction.CONFIG_CHANGED,
                        "com.hp.workpath.permission.RECEIVE_CONFIG_CHANGED_EVENT"));
        addReceiver(secondPackage, PackageContract.WorkpathEventAction.SLEEP,
                createResolveInfo(PackageContract.WorkpathEventAction.SLEEP, "wrong.permission"));

        int registeredRows = eventReceiverManager.scanInstalledPackagesAndRebuildTable();

        assertEquals(1, registeredRows);
        assertRowCountFor(testPackage, PackageContract.WorkpathEventAction.CONFIG_CHANGED, 1);
    }

    private void registerProvider(Context context) {
        EventReceiversContentProvider provider = new EventReceiversContentProvider();
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = PackageContract.EVENTRECEIVERS_AUTHORITY;
        providerInfo.exported = true;
        providerInfo.enabled = true;
        provider.attachInfo(context, providerInfo);
        ShadowContentResolver.registerProviderInternal(PackageContract.EVENTRECEIVERS_AUTHORITY, provider);
    }

    private void installPackage(String packageName) {
        shadowPackageManager.installPackage(createPackageInfo(packageName));
    }

    private void addReceiver(String packageName, String action, ResolveInfo resolveInfo) {
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        shadowPackageManager.addResolveInfoForIntent(intent, resolveInfo);
    }

    private void clearTable() {
        applicationContext.getContentResolver().delete(PackageContract.EVENTRECEIVERS_CONTENT_URI, null, null);
    }

    private void insertRow(String eventType, String packageName) {
        ContentValues values = new ContentValues();
        values.put(PackageContract.EventReceiverEntry.EVENT_TYPE, eventType);
        values.put(PackageContract.EventReceiverEntry.PACKAGE_NAME, packageName);
        applicationContext.getContentResolver().insert(PackageContract.EVENTRECEIVERS_CONTENT_URI, values);
    }

    private int getRowCount() {
        try (Cursor cursor = applicationContext.getContentResolver().query(
                     PackageContract.EVENTRECEIVERS_CONTENT_URI,
                     PackageContract.ALL_PROJECTION_EVENT_RECEIVERS,
                     null,
                     null,
                     null)) {
            return cursor != null ? cursor.getCount() : 0;
        }
    }

    private void assertRowCountFor(String packageName, String eventType, int expectedCount) {
        try (Cursor cursor = applicationContext.getContentResolver().query(
                     PackageContract.EVENTRECEIVERS_CONTENT_URI,
                     PackageContract.ALL_PROJECTION_EVENT_RECEIVERS,
                     PackageContract.EventReceiverEntry.PACKAGE_NAME + "=? AND " + PackageContract.EventReceiverEntry.EVENT_TYPE + "=?",
                     new String[]{packageName, eventType},
                     null)) {
            assertEquals(expectedCount, cursor != null ? cursor.getCount() : 0);
        }
    }

    private ResolveInfo createResolveInfo(String action, String permission) {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.filter = new IntentFilter(action);
        resolveInfo.activityInfo = new ActivityInfo();
        resolveInfo.activityInfo.permission = permission;
        return resolveInfo;
    }

    private PackageInfo createPackageInfo(String packageName) {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = packageName;
        return packageInfo;
    }

    private void resetDbHelperSingleton() throws Exception {
        Class<?> helperClass = Class.forName("com.hp.jetadvantage.link.pkgmgt.providers.PackageDBHelper");
        Field instanceField = helperClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}



