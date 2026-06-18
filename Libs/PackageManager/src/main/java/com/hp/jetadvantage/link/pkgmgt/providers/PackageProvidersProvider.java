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

public class PackageProvidersProvider extends ContentProvider {
    private static final String TAG = Constants.TAG + "ProvidersCP";

    private static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Constants.VND_HP_PROVIDERS_INFO;

    private static final int PROVIDERS_CODE = 1;

    private static final UriMatcher S_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        S_URI_MATCHER.addURI(PackageContract.PROVIDERS_AUTHORITY, null, PROVIDERS_CODE);
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

        switch (S_URI_MATCHER.match(uri)) {
            case PROVIDERS_CODE:
                SQLiteDatabase db = mPackageDbHelper.getReadableDatabase();
                return db.query(PackageDBHelper.Tables.PROVIDERS,
                        projection, selection, selectionArgs, null, null, sortOrder);

            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        switch (S_URI_MATCHER.match(uri)) {
            case PROVIDERS_CODE:
                return CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues contentValues) {
        SecurityHelper.checkCallingPackage(getContext());
        Log.d(TAG, "insert providers: " + uri.getPath());
        switch (S_URI_MATCHER.match(uri)) {
            case PROVIDERS_CODE:
                if (!contentValues.containsKey(PackageContract.PackageProviderEntry.AGENT_ID)) {
                    throw new IllegalArgumentException("Missing mandatory parameter: agentId");
                }

                if (!contentValues.containsKey(PackageContract.PackageProviderEntry.SOLUTION_ID)) {
                    throw new IllegalArgumentException("Missing mandatory parameter: solutionId");
                }

                try {
                    SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                    long insertedId = db.insertOrThrow(PackageDBHelper.Tables.PROVIDERS, null, contentValues);
                    Log.d(TAG, "Inserted a new package providers: " + insertedId);
                    return PackageContract.PROVIDERS_CONTENT_URI;
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to insert package providers into database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to insert package providers to database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());

        switch (S_URI_MATCHER.match(uri)) {
            case PROVIDERS_CODE:
                try {
                    SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                    return db.delete(PackageDBHelper.Tables.PROVIDERS, selection, selectionArgs);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to delete package configuration from database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to delete package configuration to database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues contentValues,
                      final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());

        switch (S_URI_MATCHER.match(uri)) {
            case PROVIDERS_CODE:

                if (!contentValues.containsKey(PackageContract.PackageProviderEntry.AGENT_ID)) {
                    throw new IllegalArgumentException("Missing mandatory parameter: agentId");
                }

                String agentId = contentValues.getAsString(PackageContract.PackageProviderEntry.AGENT_ID);

                SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                String querySelection = PackageContract.PackageProviderEntry.AGENT_ID + " = ?";
                String[] querySelectionArgs = new String[] {agentId};

                contentValues.remove(PackageContract.PackageProviderEntry.AGENT_ID);
                try {
                    int updated = db.update(PackageDBHelper.Tables.PROVIDERS,
                            contentValues, querySelection, querySelectionArgs);
                    return updated;
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to update package configuration from database", e);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to update package configuration to database", e);
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }
}
