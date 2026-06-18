package com.hp.jetadvantage.link.pkgmgt.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;

class PackageDBHelper extends SQLiteOpenHelper {
    private static final String TAG = Constants.TAG + "DB";

    interface Tables {
        String PACKAGES = "packages";
        String INSTALLERS = "installers";
        String PROVIDERS = "providers";
        String ATTESTATION = "attestation";
        String EVENTRECEIVERS = "eventreceivers";
    }

    private static final String SQL_CREATE_PACKAGES =
            "CREATE TABLE " + Tables.PACKAGES + " (" +
                    PackageContract.PackageEntry._ID + " INTEGER PRIMARY KEY," +
                    PackageContract.PackageEntry.APPLICATION_AGENT_ID + " TEXT," +
                    PackageContract.PackageEntry.SOLUTION_ID + " TEXT NOT NULL," +
                    PackageContract.PackageEntry.SOLUTION_NAME + " TEXT," +
                    PackageContract.PackageEntry.HPK2_VERSION + " TEXT," +
                    PackageContract.PackageEntry.APK_VERSION + " TEXT," +
                    PackageContract.PackageEntry.PACKAGE_NAME + " TEXT," +
                    PackageContract.PackageEntry.INSTALL_DATE + " INTEGER," +
                    PackageContract.PackageEntry.METADATA + " TEXT," +
                    PackageContract.PackageEntry.VENDOR_NAME + " TEXT," +
                    PackageContract.PackageEntry.LAUNCH_INTENT + " TEXT," +
                    PackageContract.PackageEntry.IS_MAIN_ACTIVITY + " BIT," +
                    PackageContract.PackageEntry.IS_HOME_SCREEN_APP + " BIT," +
                    PackageContract.PackageEntry.ALLOWLIST_TYPE + " TEXT)";

    private static final String SQL_CREATE_INSTALLERS =
            "CREATE TABLE " + Tables.INSTALLERS + " (" +
                    PackageContract.PackageInstallerEntry._ID + " INTEGER PRIMARY KEY," +
                    PackageContract.PackageInstallerEntry.SOLUTION_ID + " TEXT NOT NULL," +
                    PackageContract.PackageInstallerEntry.PACKAGE_NAME + " TEXT," +
                    PackageContract.PackageInstallerEntry.SOLUTION_NAME + " TEXT," +
                    PackageContract.PackageInstallerEntry.STATE + " TEXT NOT NULL," +
                    PackageContract.PackageInstallerEntry.ERROR + " TEXT," +
                    PackageContract.PackageInstallerEntry.LAST_UPDATED + " INTEGER)";

    private static final String SQL_CREATE_PROVIDERS =
            "CREATE TABLE " + Tables.PROVIDERS + " (" +
                    PackageContract.PackageProviderEntry._ID + " INTEGER PRIMARY KEY," +
                    PackageContract.PackageProviderEntry.SOLUTION_ID + " TEXT NOT NULL," +
                    PackageContract.PackageProviderEntry.AGENT_ID + " TEXT NOT NULL," +
                    PackageContract.PackageProviderEntry.PACKAGE_NAME + " TEXT NOT NULL," +
                    PackageContract.PackageProviderEntry.FUNCTION_TYPE + " TEXT NOT NULL," +
                    PackageContract.PackageProviderEntry.AGENT_NAME + " TEXT," +
                    PackageContract.PackageProviderEntry.AGENT_DESCRIPTION + " TEXT," +
                    PackageContract.PackageProviderEntry.LAUNCH_INTENT + " TEXT," +
                    PackageContract.PackageProviderEntry.METADATA + " TEXT," +
                    PackageContract.PackageProviderEntry.EXT_DATA1 + " TEXT," +
                    PackageContract.PackageProviderEntry.EXT_DATA2 + " TEXT," +
                    PackageContract.PackageProviderEntry.EXT_DATA3 + " TEXT)";

    private static final String SQL_CREATE_ATTESTATION =
            "CREATE TABLE " + Tables.ATTESTATION + " (" +
                    PackageContract.PackageAttestationEntry._ID + " INTEGER PRIMARY KEY," +
                    PackageContract.PackageAttestationEntry.SOLUTION_ID + " TEXT," +
                    PackageContract.PackageAttestationEntry.AUTH + " TEXT," +
                    PackageContract.PackageAttestationEntry.USER + " TEXT," +
                    '"' + PackageContract.PackageAttestationEntry.KEY + '"' + " TEXT," +
                    PackageContract.PackageAttestationEntry.DATA + " TEXT," +
                    PackageContract.PackageAttestationEntry.INSTALL_DATE + " INTEGER," +
                    PackageContract.PackageAttestationEntry.MODIFY_DATE + " INTEGER)";

    private static final String SQL_CREATE_EVENTRECEIVERS =
            "CREATE TABLE " + Tables.EVENTRECEIVERS + " (" +
                    PackageContract.EventReceiverEntry.EVENT_TYPE + " TEXT NOT NULL," +
                    PackageContract.EventReceiverEntry.PACKAGE_NAME + " TEXT NOT NULL," +
                    "PRIMARY KEY (" + PackageContract.EventReceiverEntry.EVENT_TYPE + ", " +
                    PackageContract.EventReceiverEntry.PACKAGE_NAME + "))";

    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ";

    //16 : 24.7.2, 1.2
    //17 : 24.7.3, 1.2.5
    //20 : 24.8    1.3
    //21 : 24.8.0  1.3(hotfix)
    //26 : previous
    //27 : DUNE-314377 eventreceivers table and logging updates
    //28 : DB refactoring — table schema changes
    private static final int DATABASE_VERSION = 28;
    private static final String DATABASE_NAME = "ConnectorPackages.db";

    private static PackageDBHelper instance = null;

    static synchronized PackageDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PackageDBHelper(context);
        }
        return instance;
    }

    private PackageDBHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PACKAGES);
        db.execSQL(SQL_CREATE_INSTALLERS);
        db.execSQL(SQL_CREATE_PROVIDERS);
        db.execSQL(SQL_CREATE_ATTESTATION);
        db.execSQL(SQL_CREATE_EVENTRECEIVERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Rename existing tables to backups
        db.execSQL("ALTER TABLE " + Tables.PACKAGES    + " RENAME TO packages_bak");
        db.execSQL("ALTER TABLE " + Tables.INSTALLERS  + " RENAME TO installers_bak");
        db.execSQL("ALTER TABLE " + Tables.PROVIDERS   + " RENAME TO providers_bak");
        db.execSQL("ALTER TABLE " + Tables.ATTESTATION + " RENAME TO attestation_bak");
        db.execSQL("ALTER TABLE " + Tables.EVENTRECEIVERS + " RENAME TO eventreceivers_bak");

        // Create new tables with the latest schema
        onCreate(db);

        // Copy existing data into the new tables (column names are unchanged from v28+)
        db.execSQL("INSERT OR IGNORE INTO " + Tables.PACKAGES    + " SELECT * FROM packages_bak");
        db.execSQL("INSERT OR IGNORE INTO " + Tables.INSTALLERS  + " SELECT * FROM installers_bak");
        db.execSQL("INSERT OR IGNORE INTO " + Tables.PROVIDERS   + " SELECT * FROM providers_bak");
        db.execSQL("INSERT OR IGNORE INTO " + Tables.ATTESTATION + " SELECT * FROM attestation_bak");
        db.execSQL("INSERT OR IGNORE INTO " + Tables.EVENTRECEIVERS + " SELECT * FROM eventreceivers_bak");

        // Drop backup tables
        db.execSQL(SQL_DELETE_TABLE + "packages_bak");
        db.execSQL(SQL_DELETE_TABLE + "installers_bak");
        db.execSQL(SQL_DELETE_TABLE + "providers_bak");
        db.execSQL(SQL_DELETE_TABLE + "attestation_bak");
        db.execSQL(SQL_DELETE_TABLE + "eventreceivers_bak");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion
                + "; data will be lost");
        db.execSQL(SQL_DELETE_TABLE + Tables.PACKAGES);
        db.execSQL(SQL_DELETE_TABLE + Tables.INSTALLERS);
        db.execSQL(SQL_DELETE_TABLE + Tables.PROVIDERS);
        db.execSQL(SQL_DELETE_TABLE + Tables.ATTESTATION);
        db.execSQL(SQL_DELETE_TABLE + Tables.EVENTRECEIVERS);
        onCreate(db);
    }
}
