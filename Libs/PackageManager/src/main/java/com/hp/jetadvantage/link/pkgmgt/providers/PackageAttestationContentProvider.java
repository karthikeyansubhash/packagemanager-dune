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
import android.text.TextUtils;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.exception.OutOfStorageException;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;

public class PackageAttestationContentProvider extends ContentProvider {
    private static final String TAG = Constants.TAG + "AttestationCP";

    private static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Constants.VND_HP_ATTESTATION_INFO;

    private static final int PACKAGES_CODE    = 1;
    private static final int PACKAGE_CODE     = 2;

    private static final UriMatcher S_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        S_URI_MATCHER.addURI(PackageContract.PACKAGES_ATTESTATION_AUTHORITY, null, PACKAGES_CODE);
        S_URI_MATCHER.addURI(PackageContract.PACKAGES_ATTESTATION_AUTHORITY, "*", PACKAGE_CODE);
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
            case PACKAGE_CODE:
                String uuid = uri.getLastPathSegment();
                Log.d(TAG, "Querying for attestation uuid = " + uuid);

                // for sub-app - get config for parent UUID
                String querySelection = PackageContract.PackageAttestationEntry.SOLUTION_ID + " = ?";
                String[] querySelectionArgs = new String[] {uuid};

                SQLiteDatabase db = mPackageDbHelper.getReadableDatabase();
                return db.query(PackageDBHelper.Tables.ATTESTATION,
                        projection, querySelection, querySelectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGES_CODE:
            case PACKAGE_CODE:
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
                if (!contentValues.containsKey(PackageContract.PackageAttestationEntry.SOLUTION_ID)
                        || TextUtils.isEmpty(contentValues.getAsString(PackageContract.PackageAttestationEntry.SOLUTION_ID))) {
                    throw new IllegalArgumentException("Provided value for SOLUTION_ID doesn't exist");
                }
                final String uuid = contentValues.getAsString(PackageContract.PackageAttestationEntry.SOLUTION_ID);

                try {
                    SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                    long insertedId = db.insertOrThrow(PackageDBHelper.Tables.ATTESTATION, null, contentValues);
                    Log.d(TAG, "Inserted a new attestation " + uuid + " at " + insertedId);
                    return Uri.withAppendedPath(PackageContract.PACKAGES_ATTESTATION_CONTENT_URI, uuid);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to insert attestation into database " + e.getMessage());
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to insert attestation to database " + e.getMessage());
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        SecurityHelper.checkCallingPackage(getContext());

        switch (S_URI_MATCHER.match(uri)) {
            case PACKAGE_CODE:
                try {
                    SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                    String querySelection = PackageContract.PackageAttestationEntry.SOLUTION_ID + " = ?";
                    String[] querySelectionArgs = new String[] { uri.getLastPathSegment() };
                    return db.delete(PackageDBHelper.Tables.ATTESTATION, querySelection, querySelectionArgs);
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to delete attestation into database " + e.getMessage());
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to delete attestation to database " + e.getMessage());
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
            case PACKAGE_CODE:
                final String uuid = uri.getLastPathSegment();

                // if input values contains SOLUTION_ID - check whether it's the same as in URI
                if (contentValues.containsKey(PackageContract.PackageAttestationEntry.SOLUTION_ID)
                        && !uuid.equals(contentValues.getAsString(PackageContract.PackageAttestationEntry.SOLUTION_ID))) {
                    throw new IllegalArgumentException("Provided value for SOLUTION_ID doesn't match URI");
                }

                contentValues.remove(PackageContract.PackageAttestationEntry.SOLUTION_ID);

                SQLiteDatabase db = mPackageDbHelper.getWritableDatabase();
                String querySelection = PackageContract.PackageAttestationEntry.SOLUTION_ID + " = ?";
                String[] querySelectionArgs = new String[] {uuid};

                try {
                    int updated = db.update(PackageDBHelper.Tables.ATTESTATION,
                            contentValues, querySelection, querySelectionArgs);
                    return updated;
                } catch (SQLiteFullException e) {
                    throw new OutOfStorageException("No space to update attestation into database " + e.getMessage());
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to update attestation to database " + e.getMessage());
                }
            default:
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }
    }
}
