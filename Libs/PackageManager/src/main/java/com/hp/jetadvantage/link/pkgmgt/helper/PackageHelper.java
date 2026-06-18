package com.hp.jetadvantage.link.pkgmgt.helper;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;
import com.hp.jetadvantage.link.pkgmgt.model.PackageInstallerState;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PackageHelper {
    private static final String TAG = Constants.TAG + "PackageHelper";

    public static void processIncompleteInstallers(final Context context) {
        try {
            String selection = PackageContract.PackageInstallerEntry.STATE + " IN (?)";
            String[] selectionArgs = new String[]{
                    PackageInstallerState.isInProgress.name()
            };

            Cursor cursor = PackageManagerApplication.getAppContext().getContentResolver()
                    .query(PackageContract.INSTALLERS_CONTENT_URI, null, selection, selectionArgs, null);

            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(PackageContract.PackageInstallerEntry._ID));
                        String solutionId = cursor.getString(cursor.getColumnIndexOrThrow(PackageContract.PackageInstallerEntry.SOLUTION_ID));

                        Log.d(TAG, "Checking installer for " + solutionId);
                        String pkgSelection = PackageContract.PackageEntry.SOLUTION_ID + " = ?";
                        String[] pkgSelectionArgs = new String[]{ solutionId };
                        Cursor pkgCursor = PackageManagerApplication.getAppContext().getContentResolver()
                                .query(PackageContract.PACKAGES_CONTENT_URI, null, pkgSelection, pkgSelectionArgs, null);
                        if (pkgCursor != null) {
                            try {
                                PackageInstallerState state;
                                boolean success = pkgCursor.moveToNext(); // package exists
                                Log.d(TAG, "Is package " + solutionId + " exists in database: " + success);

                                if (success) {
                                    // move state to Completed
                                    state = PackageInstallerState.isCompleted;
                                } else {
                                    // package doesn't exist - move state to Failed
                                    state = PackageInstallerState.isFailed;
                                }

                                DatabaseHelper.updateInstallerState(id, state, null);
                            } finally {
                                pkgCursor.close();
                            }
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "IncompleteInstallers has an error: " + e.getMessage());
        }

    }

    public static void cleanupExpiredInstallers(Context context) {
        Log.d(TAG, "Cleaning up all existing installers older than 1 day");

        try {
            // remove all installers with lastUpdated less than current time minus 1 day
            String selection = "DATETIME(" + PackageContract.PackageInstallerEntry.LAST_UPDATED + "/1000, 'unixepoch') < DATE('now', '-1 day')";

            int removed = context.getContentResolver().delete(PackageContract.INSTALLERS_CONTENT_URI,
                    selection, null);
            Log.d(TAG, "Removed expired installers: " + removed);
        } catch (Exception e) {
            Log.e(TAG, "Failed to clean up expired installers:" + e.getMessage(), e);
        }
    }

    public static String getApplicationHash(final String path) {
        final String encrypt = "SHA-256";

        if(TextUtils.isEmpty(path) || !(new File(path)).exists()) {
            Log.e(TAG, "ApplicationHash: package path is null or invalid path");
            return null;
        }

        String hashString = null;
        BufferedInputStream bis= null;

        try {
            MessageDigest digest = MessageDigest.getInstance(encrypt);
            byte[] buffer = new byte[512 * 1024];
            int count;
            bis = new BufferedInputStream(new FileInputStream(path));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();

            byte[] hash = digest.digest();
            hashString = Base64.encodeToString(hash, Base64.NO_WRAP).trim();
        } catch (final NoSuchAlgorithmException nse) {
            Log.e(TAG, "algorithm is not supported", nse);
        } catch (final IOException ioe) {
            Log.e(TAG, "package read error", ioe);
        } finally {
            if(bis != null) { try{ bis.close(); } catch (IOException ignored) {} }
        }

        return hashString;
    }

    public static void cleanTemporaryFolder(File folder) {
        try {
            if (folder != null && folder.exists() && folder.isDirectory()) {
                final File[] files = folder.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        // remove folder contents first
                        if (file.isDirectory()) {
                            cleanTemporaryFolder(file);
                        }

                        if (!file.delete()) {
                            Log.w(TAG, "Failed to delete " + file.getName());
                        } else {
                            Log.i(TAG, "File is deleted");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to clean folder:" + e.getMessage(), e);
        }
    }
}
