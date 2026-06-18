package com.hp.jetadvantage.link.pkgmgt.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.hp.jetadvantage.link.pkgmgt.PackageContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 31)
public class PackageDBHelperUpgradeTest {

    private Context context;
    private PackageDBHelper helper;
    private SQLiteDatabase db;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        resetDbHelperSingleton();
        context.deleteDatabase("ConnectorPackages.db");

        helper = PackageDBHelper.getInstance(context);
        db = helper.getWritableDatabase();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
        resetDbHelperSingleton();
        context.deleteDatabase("ConnectorPackages.db");
    }

    // ── onUpgrade tests ───────────────────────────────────────────────────────

    @Test
    public void onUpgrade_packages_dataIsPreserved() {
        insertPackage("sol-001", "com.hp.app.one");
        insertPackage("sol-002", "com.hp.app.two");

        helper.onUpgrade(db, 28, 29);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.PACKAGES,
                new String[]{PackageContract.PackageEntry.SOLUTION_ID,
                        PackageContract.PackageEntry.PACKAGE_NAME},
                null, null, null, null,
                PackageContract.PackageEntry.SOLUTION_ID + " ASC")) {
            assertEquals(2, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals("sol-001", cursor.getString(0));
            assertEquals("com.hp.app.one", cursor.getString(1));
            assertTrue(cursor.moveToNext());
            assertEquals("sol-002", cursor.getString(0));
            assertEquals("com.hp.app.two", cursor.getString(1));
        }
    }

    @Test
    public void onUpgrade_installers_dataIsPreserved() {
        insertInstaller("sol-ins-001", "INSTALLED");
        insertInstaller("sol-ins-002", "PENDING");

        helper.onUpgrade(db, 28, 29);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.INSTALLERS,
                new String[]{PackageContract.PackageInstallerEntry.SOLUTION_ID,
                        PackageContract.PackageInstallerEntry.STATE},
                null, null, null, null,
                PackageContract.PackageInstallerEntry.SOLUTION_ID + " ASC")) {
            assertEquals(2, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals("sol-ins-001", cursor.getString(0));
            assertEquals("INSTALLED", cursor.getString(1));
            assertTrue(cursor.moveToNext());
            assertEquals("sol-ins-002", cursor.getString(0));
            assertEquals("PENDING", cursor.getString(1));
        }
    }

    @Test
    public void onUpgrade_providers_dataIsPreserved() {
        insertProvider("sol-001", "agent-001", "com.hp.app.one", "PRINT");
        insertProvider("sol-001", "agent-002", "com.hp.app.one", "SCAN");

        helper.onUpgrade(db, 28, 29);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.PROVIDERS,
                new String[]{PackageContract.PackageProviderEntry.AGENT_ID,
                        PackageContract.PackageProviderEntry.FUNCTION_TYPE},
                null, null, null, null,
                PackageContract.PackageProviderEntry.AGENT_ID + " ASC")) {
            assertEquals(2, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals("agent-001", cursor.getString(0));
            assertEquals("PRINT", cursor.getString(1));
            assertTrue(cursor.moveToNext());
            assertEquals("agent-002", cursor.getString(0));
            assertEquals("SCAN", cursor.getString(1));
        }
    }

    @Test
    public void onUpgrade_attestation_dataIsPreserved() {
        insertAttestation("sol-001", "auth-token-abc");

        helper.onUpgrade(db, 28, 29);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.ATTESTATION,
                new String[]{PackageContract.PackageAttestationEntry.SOLUTION_ID,
                        PackageContract.PackageAttestationEntry.AUTH},
                null, null, null, null, null)) {
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals("sol-001", cursor.getString(0));
            assertEquals("auth-token-abc", cursor.getString(1));
        }
    }

    @Test
    public void onUpgrade_eventreceivers_dataIsPreserved() {
        insertEventReceiver(PackageContract.WorkpathEventAction.SIGN_IN, "com.hp.app.one");
        insertEventReceiver(PackageContract.WorkpathEventAction.SIGN_OUT, "com.hp.app.two");

        helper.onUpgrade(db, 28, 29);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.EVENTRECEIVERS,
                new String[]{PackageContract.EventReceiverEntry.EVENT_TYPE,
                        PackageContract.EventReceiverEntry.PACKAGE_NAME},
                null, null, null, null,
                PackageContract.EventReceiverEntry.EVENT_TYPE + " ASC")) {
            assertEquals(2, cursor.getCount());
        }
    }

    @Test
    public void onUpgrade_multipleTimesInSequence_dataIsPreserved() {
        insertPackage("sol-multi", "com.hp.app.multi");

        helper.onUpgrade(db, 28, 29);
        helper.onUpgrade(db, 29, 30);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.PACKAGES,
                new String[]{PackageContract.PackageEntry.SOLUTION_ID},
                null, null, null, null, null)) {
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals("sol-multi", cursor.getString(0));
        }
    }

    // ── onDowngrade tests ─────────────────────────────────────────────────────

    @Test
    public void onDowngrade_packages_tableIsEmptyAfterDowngrade() {
        insertPackage("sol-down-001", "com.hp.app.down");

        helper.onDowngrade(db, 29, 28);

        try (Cursor cursor = db.query(PackageDBHelper.Tables.PACKAGES,
                null, null, null, null, null, null)) {
            assertEquals(0, cursor.getCount());
        }
    }

    @Test
    public void onDowngrade_allTables_existAndAreEmpty() {
        insertPackage("sol-down-001", "com.hp.app.one");
        insertInstaller("sol-ins-down-001", "INSTALLED");
        insertProvider("sol-down-001", "agent-down-001", "com.hp.app.one", "PRINT");
        insertAttestation("sol-down-001", "auth-xyz");
        insertEventReceiver(PackageContract.WorkpathEventAction.WAKE_UP, "com.hp.app.one");

        helper.onDowngrade(db, 29, 28);

        assertTableIsEmpty(PackageDBHelper.Tables.PACKAGES);
        assertTableIsEmpty(PackageDBHelper.Tables.INSTALLERS);
        assertTableIsEmpty(PackageDBHelper.Tables.PROVIDERS);
        assertTableIsEmpty(PackageDBHelper.Tables.ATTESTATION);
        assertTableIsEmpty(PackageDBHelper.Tables.EVENTRECEIVERS);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void insertPackage(String solutionId, String packageName) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageEntry.SOLUTION_ID, solutionId);
        cv.put(PackageContract.PackageEntry.PACKAGE_NAME, packageName);
        db.insertOrThrow(PackageDBHelper.Tables.PACKAGES, null, cv);
    }

    private void insertInstaller(String solutionId, String state) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageInstallerEntry.SOLUTION_ID, solutionId);
        cv.put(PackageContract.PackageInstallerEntry.STATE, state);
        db.insertOrThrow(PackageDBHelper.Tables.INSTALLERS, null, cv);
    }

    private void insertProvider(String solutionId, String agentId,
                                String packageName, String functionType) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageProviderEntry.SOLUTION_ID, solutionId);
        cv.put(PackageContract.PackageProviderEntry.AGENT_ID, agentId);
        cv.put(PackageContract.PackageProviderEntry.PACKAGE_NAME, packageName);
        cv.put(PackageContract.PackageProviderEntry.FUNCTION_TYPE, functionType);
        db.insertOrThrow(PackageDBHelper.Tables.PROVIDERS, null, cv);
    }

    private void insertAttestation(String solutionId, String auth) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageAttestationEntry.SOLUTION_ID, solutionId);
        cv.put(PackageContract.PackageAttestationEntry.AUTH, auth);
        db.insertOrThrow(PackageDBHelper.Tables.ATTESTATION, null, cv);
    }

    private void insertEventReceiver(String eventType, String packageName) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.EventReceiverEntry.EVENT_TYPE, eventType);
        cv.put(PackageContract.EventReceiverEntry.PACKAGE_NAME, packageName);
        db.insertOrThrow(PackageDBHelper.Tables.EVENTRECEIVERS, null, cv);
    }

    private void assertTableIsEmpty(String tableName) {
        try (Cursor cursor = db.query(tableName, null, null, null, null, null, null)) {
            assertEquals("Expected " + tableName + " to be empty after downgrade",
                    0, cursor.getCount());
        }
    }

    private void resetDbHelperSingleton() throws Exception {
        Field instanceField = PackageDBHelper.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
