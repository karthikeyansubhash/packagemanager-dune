package com.hp.jetadvantage.link.pkgmgt.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
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

public class PackageContentProvider extends ContentProvider {
    private static final String TAG = Constants.TAG + "PackageCP";

    private static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + Constants.VND_HP_PACKAGE_INFO;

    private static final int PACKAGES_CODE         = 1;
    private static final int PACKAGE_CODE           = 2;
    private static final int PACKAGES_SOLUTION_CODE = 4;

    private static final UriMatcher S_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    // NOTE: The order of addURI() calls matters on some Android devices.
    // On certain OEM implementations, UriMatcher stops at the first wildcard ("*") match
    // without continuing to check more specific (EXACT) patterns registered afterward.
    // Always register exact/specific patterns BEFORE wildcard patterns to ensure correct matching.
    static {
        S_URI_MATCHER.addURI(PackageContract.PACKAGES_AUTHORITY, null, PACKAGES_CODE);
        S_URI_MATCHER.addURI(PackageContract.PACKAGES_AUTHORITY, PackageContract.PACKAGES_SOLUTION_PATH_SEGMENT + "/*", PACKAGES_SOLUTION_CODE);
        S_URI_MATCHER.addURI(PackageContract.PACKAGES_AUTHORITY, "*", PACKAGE_CODE);
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

        SQLiteDatabase db;
        String[] queryProjection = projection;
        if (queryProjection == null) {
            queryProjection = PackageContract.DEFAULT_PROJECTION_PACKAGE;
        }

        String querySelection = selection;
        String[] querySelectionArgs = selectionArgs;

        db = mPackageDbHelper.getReadableDatabase();

        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGES_SOLUTION_CODE:
                String solutionId = uri.getLastPathSegment();
                Log.d(TAG, "Querying for package solutionId = " + solutionId);
                querySelection = PackageContract.PackageEntry.SOLUTION_ID + " = ?";
                querySelectionArgs = new String[] {solutionId};
                break;

            case PACKAGE_CODE:
                String agentId = uri.getLastPathSegment();
                Log.d(TAG, "Querying for package agentId = " + agentId);
                querySelection = PackageContract.PackageEntry.APPLICATION_AGENT_ID + " = ?";
                querySelectionArgs = new String[] {agentId};
                break;

            case PACKAGES_CODE:
                break;

            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
        Log.d(TAG, "Querying for package querySelection = " + querySelection);
        Log.d(TAG, "Querying for package querySelectionArgs = " + java.util.Arrays.toString(querySelectionArgs));
        return db.query(PackageDBHelper.Tables.PACKAGES, queryProjection, querySelection, querySelectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGES_CODE:
            case PACKAGE_CODE:
            case PACKAGES_SOLUTION_CODE:
                return CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues contentValues) {
        SecurityHelper.checkCallingPackage(getContext());

        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGES_CODE:
                SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                String agentId = contentValues.getAsString(PackageContract.PackageEntry.APPLICATION_AGENT_ID);
                try {
                    long insertedId = db.insertOrThrow(PackageDBHelper.Tables.PACKAGES, null, contentValues);
                    Log.d(TAG, "Inserted a new package " + agentId + " at " + insertedId);
                    return Uri.withAppendedPath(PackageContract.PACKAGES_CONTENT_URI, agentId);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to insert package into database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to insert package to database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());
        SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();

        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGES_CODE:
                try {
                    return db.delete(PackageDBHelper.Tables.PACKAGES, selection, selectionArgs);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to delete package from database", e);
                }
            case PACKAGE_CODE:
                Log.e(TAG, "PACKAGE_CODE: " + uri.getLastPathSegment());
                try {
                    String querySelection = PackageContract.PackageEntry.APPLICATION_AGENT_ID + " = ?";
                    String[] querySelectionArgs = new String[] { uri.getLastPathSegment() };
                    return db.delete(PackageDBHelper.Tables.PACKAGES, querySelection, querySelectionArgs);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to delete package from database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to delete package from database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues contentValues, final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());
        SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGES_CODE:
                try {
                    return db.update(PackageDBHelper.Tables.PACKAGES, contentValues, selection, selectionArgs);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to delete package from database", e);
                }
            case PACKAGE_CODE:
                try {
                    String querySelection = PackageContract.PackageEntry.APPLICATION_AGENT_ID + " = ?";
                    String[] querySelectionArgs = new String[] { uri.getLastPathSegment() };
                    contentValues.remove(PackageContract.PackageEntry.APPLICATION_AGENT_ID);  // not allowed to update agent ID
                    return db.update(PackageDBHelper.Tables.PACKAGES, contentValues, querySelection, querySelectionArgs);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to update package from database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to update package from database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }
}
