package com.hp.jetadvantage.link.pkgmgt.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;

/**
 * ContentProvider for EventReceivers table.
 * Exposes query interface for ServicesApp to retrieve registered packages by eventType.
 * Allows insertion and deletion of event receiver registrations.
 */
public class EventReceiversContentProvider extends ContentProvider {
    private static final String TAG = Constants.TAG + "EventReceiversProvider";
    private static final String TABLE_EVENTRECEIVERS = "eventreceivers";

    private static final int EVENTRECEIVERS = 100;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // JOLT-style single URI endpoint, with legacy authority compatibility.
        uriMatcher.addURI(PackageContract.EVENTRECEIVERS_AUTHORITY, null, EVENTRECEIVERS);
        uriMatcher.addURI(PackageContract.EVENT_RECEIVER_AUTHORITY, null, EVENTRECEIVERS);
    }

    private PackageDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = PackageDBHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SecurityHelper.checkCallingPackageForLink(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            if (uriMatcher.match(uri) != EVENTRECEIVERS) {
                throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
            }

            Cursor cursor;
            // JOLT behavior: selectionArgs[0] is eventType when selection is null.
            if (selection == null && selectionArgs != null && selectionArgs.length > 0) {
                String eventType = selectionArgs[0];
                cursor = db.query(
                        TABLE_EVENTRECEIVERS,
                        new String[]{PackageContract.EventReceiverEntry.PACKAGE_NAME},
                        PackageContract.EventReceiverEntry.EVENT_TYPE + " = ?",
                        new String[]{eventType},
                        null,
                        null,
                        sortOrder);
            } else {
                // Keep generic query support for internal debug/maintenance use.
                cursor = db.query(TABLE_EVENTRECEIVERS, projection, selection, selectionArgs,
                        null, null, sortOrder);
            }

            if (cursor != null && getContext() != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
            return cursor;
        } catch (Exception e) {
            Log.e(TAG, "Error querying event receivers: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.hp.eventreceivers";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SecurityHelper.checkCallingPackageForLink(getContext());
        if (uriMatcher.match(uri) != EVENTRECEIVERS) {
            throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // JOLT behavior: if row already exists, ignore insertion.
            long rowId = db.insertWithOnConflict(TABLE_EVENTRECEIVERS, null, values,
                    SQLiteDatabase.CONFLICT_IGNORE);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowId == -1 ? uri : Uri.withAppendedPath(uri, String.valueOf(rowId));
        } catch (Exception e) {
            Log.e(TAG, "Error inserting event receiver: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SecurityHelper.checkCallingPackageForLink(getContext());
        if (uriMatcher.match(uri) != EVENTRECEIVERS) {
            throw new IllegalArgumentException("Unknown or invalid URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows;
            // JOLT behavior: delete by packageName in selectionArgs[0].
            if (selectionArgs != null && selectionArgs.length > 0) {
                deletedRows = db.delete(TABLE_EVENTRECEIVERS,
                        PackageContract.EventReceiverEntry.PACKAGE_NAME + " = ?",
                        new String[]{selectionArgs[0]});
            } else {
                // Keep table cleanup behavior used by internal rebuild flows.
                deletedRows = db.delete(TABLE_EVENTRECEIVERS, selection, selectionArgs);
            }

            if (deletedRows > 0 && getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return deletedRows;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting event receivers: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // Not needed for this use case.
        return 0;
    }
}
