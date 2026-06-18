package com.hp.jetadvantage.link.pkgmgt.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.exception.OutOfStorageException;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;

public class PackageInstallerContentProvider extends ContentProvider {
    private static final String TAG = Constants.TAG + "InstallerCP";

    private static final String INSTALLER_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + Constants.VND_HP_INSTALLER_INFO;

    private static final int INSTALLERS_CODE = 1;
    private static final int INSTALLER_CODE = 2;

    private static final UriMatcher S_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        S_URI_MATCHER.addURI(PackageContract.INSTALLERS_AUTHORITY, null, INSTALLERS_CODE);
        S_URI_MATCHER.addURI(PackageContract.INSTALLERS_AUTHORITY, "#", INSTALLER_CODE);
    }

    private PackageDBHelper mPackageDbHelper;

    @Override
    public boolean onCreate() {
        mPackageDbHelper = PackageDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection,
                        final String selection, final String[] selectionArgs, final String sortOrder) {
        SecurityHelper.checkCallingPackageForLink(getContext());

        String querySelection = selection;
        String[] querySelectionArgs = selectionArgs;

        switch (S_URI_MATCHER.match(uri)) {
            case INSTALLER_CODE:
                Log.d(TAG, "Querying for installer id = " + ContentUris.parseId(uri));
                querySelection = PackageContract.PackageInstallerEntry._ID + " = ?";
                querySelectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
            case INSTALLERS_CODE:
                SQLiteDatabase db = mPackageDbHelper.getReadableDatabase();
                return db.query(PackageDBHelper.Tables.INSTALLERS, projection, querySelection, querySelectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        switch (S_URI_MATCHER.match(uri)) {
            case INSTALLERS_CODE:
            case INSTALLER_CODE:
                return INSTALLER_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues contentValues) {
        SecurityHelper.checkCallingPackage(getContext());

        switch (S_URI_MATCHER.match(uri)) {
            case INSTALLERS_CODE:
                if (!contentValues.containsKey(PackageContract.PackageInstallerEntry.LAST_UPDATED)) {
                    contentValues.put(PackageContract.PackageInstallerEntry.LAST_UPDATED, System.currentTimeMillis());
                }

                SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                String solutionId = contentValues.getAsString(PackageContract.PackageInstallerEntry.SOLUTION_ID);
                try {
                    long insertedId = db.insertOrThrow(PackageDBHelper.Tables.INSTALLERS, null, contentValues);
                    Log.d(TAG, "Inserted a new package installer " + solutionId + " at " + insertedId);
                    return ContentUris.withAppendedId(PackageContract.INSTALLERS_CONTENT_URI, insertedId);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to insert package installer into database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to insert package installer to database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());

        String querySelection = selection;
        String[] querySelectionArgs = selectionArgs;
        switch (S_URI_MATCHER.match(uri)) {
            case INSTALLER_CODE:
                querySelection = PackageContract.PackageInstallerEntry._ID + " = ?";
                querySelectionArgs = new String[] { uri.getLastPathSegment() };
            case INSTALLERS_CODE:
                Log.d(TAG, "selection: " + querySelection);
                if (querySelectionArgs != null)
                    Log.d(TAG, "selectionArgs: " + querySelectionArgs[0]);
                try {
                    SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                    return db.delete(PackageDBHelper.Tables.INSTALLERS, querySelection, querySelectionArgs);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to delete package installer from database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to delete package installer from database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues contentValues, final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());

        String querySelection = selection;
        String[] querySelectionArgs = selectionArgs;
        switch (S_URI_MATCHER.match(uri)) {
            case INSTALLER_CODE:
                Log.e(TAG,"INSTALLER_CODE");
                querySelection = PackageContract.PackageInstallerEntry._ID + " = ?";
                querySelectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
            case INSTALLERS_CODE:
                try {
                    Log.e(TAG,"INSTALLERS_CODE");
                    SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                    return db.update(PackageDBHelper.Tables.INSTALLERS, contentValues, querySelection, querySelectionArgs);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to update package installer from database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to update package installer from database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }
}
