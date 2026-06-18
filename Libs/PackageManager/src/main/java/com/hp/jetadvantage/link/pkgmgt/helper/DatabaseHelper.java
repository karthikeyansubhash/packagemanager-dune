package com.hp.jetadvantage.link.pkgmgt.helper;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hp.ext.service.application.ApplicationAgentRegistrationRecord;
import com.hp.ext.types.solutionManager.SolutionContent;
import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;
import com.hp.jetadvantage.link.pkgmgt.model.Error;
import com.hp.jetadvantage.link.pkgmgt.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.ApkInfo;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions.Solutions;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper {
    private static final String TAG = Constants.TAG + "DuneDBHelper";

    // ─── Packages Table ───────────────────────────────────────────────────────

    public static ContentValues buildPackageValues(ApkInfo apkInfo, SolutionContent solutionContent,
                                                   ApplicationAgentRegistrationRecord record) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageEntry.APPLICATION_AGENT_ID, record.getAgentId().toString());
        cv.put(PackageContract.PackageEntry.SOLUTION_NAME, record.getName().toString());
        cv.put(PackageContract.PackageEntry.HPK2_VERSION, solutionContent.getDescription().getVersion());
        cv.put(PackageContract.PackageEntry.APK_VERSION, apkInfo.getVersionName());
        cv.put(PackageContract.PackageEntry.PACKAGE_NAME, apkInfo.getPackageName());
        cv.put(PackageContract.PackageEntry.INSTALL_DATE, System.currentTimeMillis());
        cv.put(PackageContract.PackageEntry.VENDOR_NAME, solutionContent.getDescription().getVendor());
        cv.put(PackageContract.PackageEntry.SOLUTION_ID, solutionContent.getSolutionId().toString());
        return cv;
    }

    public static ContentValues buildPackageValues(String packageName, SolutionContent solutionContent) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageEntry.SOLUTION_NAME, solutionContent.getDescription().getName());
        cv.put(PackageContract.PackageEntry.HPK2_VERSION, solutionContent.getDescription().getVersion());
        cv.put(PackageContract.PackageEntry.PACKAGE_NAME, packageName);
        cv.put(PackageContract.PackageEntry.INSTALL_DATE, System.currentTimeMillis());
        cv.put(PackageContract.PackageEntry.VENDOR_NAME, solutionContent.getDescription().getVendor());
        cv.put(PackageContract.PackageEntry.SOLUTION_ID, solutionContent.getSolutionId().toString());
        return cv;
    }

    @SuppressLint("Range")
    public static String queryMainApplicationId(String solutionId) {
        Context context = PackageManagerApplication.getAppContext();
        String selection = PackageContract.PackageEntry.SOLUTION_ID + " = ? AND " +
                PackageContract.PackageEntry.IS_MAIN_ACTIVITY + " = ?";
        String[] selectionArgs = {solutionId, "1"};

        Cursor cursor = context.getContentResolver().query(PackageContract.PACKAGES_CONTENT_URI,
                null, selection, selectionArgs, null);
        try {
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(PackageContract.PackageEntry.APPLICATION_AGENT_ID));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    @NonNull
    public static String upsertPackage(String solutionId, String agentId, ContentValues cv) {
        Context context = PackageManagerApplication.getAppContext();
        String action;
        String selection;
        String[] args;

        if (agentId != null) {
            selection = PackageContract.PackageEntry.SOLUTION_ID + " = ? AND " +
                    PackageContract.PackageEntry.APPLICATION_AGENT_ID + " = ?";
            args = new String[]{solutionId, agentId};
        } else {
            selection = PackageContract.PackageEntry.SOLUTION_ID + " = ?";
            args = new String[]{solutionId};
        }

        Cursor packageCursor = context.getContentResolver().query(
                PackageContract.PACKAGES_CONTENT_URI, null, selection, args, null);
        try {
            if (packageCursor != null && packageCursor.moveToNext()) {
                int updated = context.getContentResolver().update(
                        PackageContract.PACKAGES_CONTENT_URI, cv, selection, args);
                if (updated > 0) {
                    Log.i(TAG, "Package is successfully updated " + updated);
                    action = PackageContract.Intent.ACTION_PACKAGE_UPDATED;
                } else {
                    Log.e(TAG, "Package is failed to be updated");
                    throw new IllegalStateException("Failed to update package solutionId=" + solutionId);
                }
            } else {
                Uri packageInserted = context.getContentResolver().insert(PackageContract.PACKAGES_CONTENT_URI, cv);
                if (packageInserted != null) {
                    Log.i(TAG, "Package is successfully created at " + packageInserted);
                    action = PackageContract.Intent.ACTION_PACKAGE_INSTALLED;
                } else {
                    Log.e(TAG, "Package is failed to be created (" + cv + ")");
                    throw new IllegalStateException("Failed to insert package");
                }
            }
        } finally {
            if (packageCursor != null) packageCursor.close();
        }
        return action;
    }

    public static int updatePackage(ContentValues cv, String where, String[] selectionArgs) {
        Context context = PackageManagerApplication.getAppContext();
        return context.getContentResolver().update(PackageContract.PACKAGES_CONTENT_URI, cv, where, selectionArgs);
    }

    public static boolean deletePackageRecord(String solutionId) {
        Context context = PackageManagerApplication.getAppContext();
        String selection = PackageContract.PackageEntry.SOLUTION_ID + " = ?";
        String[] selectionArgs = {solutionId};
        return context.getContentResolver().delete(PackageContract.PACKAGES_CONTENT_URI, selection, selectionArgs) > 0;
    }

    // ─── Installers Table ────────────────────────────────────────────────────

    public static ContentValues buildInstallerValues(SolutionContent solutionContent, String packageName,
                                                     PackageInstallerState status, String message) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageInstallerEntry.SOLUTION_ID, solutionContent.getSolutionId().toString());
        cv.put(PackageContract.PackageInstallerEntry.STATE, status.name());
        cv.put(PackageContract.PackageInstallerEntry.LAST_UPDATED, new Date().getTime());
        cv.put(PackageContract.PackageInstallerEntry.SOLUTION_NAME, solutionContent.getDescription().getName());
        cv.put(PackageContract.PackageInstallerEntry.PACKAGE_NAME, packageName);
        if (!TextUtils.isEmpty(message)) {
            cv.put(PackageContract.PackageInstallerEntry.ERROR, message);
        }
        return cv;
    }

    public static ContentValues buildInstallerValues(Solutions solutions, String packageName,
                                                     PackageInstallerState status, String message) {
        ContentValues cv = new ContentValues();
        cv.put(PackageContract.PackageInstallerEntry.SOLUTION_ID, solutions.getSolutionId());
        cv.put(PackageContract.PackageInstallerEntry.STATE, status.name());
        cv.put(PackageContract.PackageInstallerEntry.LAST_UPDATED, new Date().getTime());
        cv.put(PackageContract.PackageInstallerEntry.SOLUTION_NAME, solutions.getDescription().getName());
        cv.put(PackageContract.PackageInstallerEntry.PACKAGE_NAME, packageName);
        if (!TextUtils.isEmpty(message)) {
            cv.put(PackageContract.PackageInstallerEntry.ERROR, message);
        }
        return cv;
    }

    @SuppressLint("Range")
    public static ArrayList<ContentValues> queryInstallerSeedFromPackages(String solutionId) {
        Context context = PackageManagerApplication.getAppContext();
        String selection = PackageContract.PackageEntry.SOLUTION_ID + " = ?";
        String[] selectionArgs = {solutionId};
        ArrayList<ContentValues> cvList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(PackageContract.PACKAGES_CONTENT_URI,
                null, selection, selectionArgs, null);
        try {
            if (cursor != null && cursor.moveToNext()) {
                String applicationAgentId = cursor.getString(cursor.getColumnIndex(PackageContract.PackageEntry.APPLICATION_AGENT_ID));
                String appName = cursor.getString(cursor.getColumnIndex(PackageContract.PackageEntry.SOLUTION_NAME));
                String packageName = cursor.getString(cursor.getColumnIndex(PackageContract.PackageEntry.PACKAGE_NAME));

                Log.i(TAG, "Package found: solutionId=" + solutionId + ", name=" + appName + ", applicationAgentId=" + applicationAgentId);

                ContentValues cv = new ContentValues();
                cv.put(PackageContract.PackageInstallerEntry.SOLUTION_ID, solutionId);
                cv.put(PackageContract.PackageInstallerEntry.SOLUTION_NAME, appName);
                cv.put(PackageContract.PackageInstallerEntry.PACKAGE_NAME, packageName);
                cvList.add(cv);
            }
            return cvList;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public static void upsertInstaller(ContentValues cv) {
        Context context = PackageManagerApplication.getAppContext();
        String solutionId = cv.getAsString(PackageContract.PackageInstallerEntry.SOLUTION_ID);
        String selection = PackageContract.PackageInstallerEntry.SOLUTION_ID + " = ?";
        String[] selectionArgs = new String[]{solutionId};

        Log.i(TAG, "upsertInstaller solutionId: " + solutionId);
        Cursor cursor = context.getContentResolver().query(
                PackageContract.INSTALLERS_CONTENT_URI, null, selection, selectionArgs, null);
        try {
            if (cursor != null && cursor.moveToNext()) {
                int updated = context.getContentResolver().update(
                        PackageContract.INSTALLERS_CONTENT_URI, cv, selection, selectionArgs);
                Log.i(TAG, "Installer is updated " + updated);
            } else {
                Uri insert = context.getContentResolver().insert(
                        PackageContract.INSTALLERS_CONTENT_URI, cv);
                Log.i(TAG, "Installer is inserted " + insert);
            }
        } catch (Exception e) {
            Log.e(TAG, "upsertInstaller exception", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public static boolean deleteInstaller(String solutionId) {
        Context context = PackageManagerApplication.getAppContext();
        String selection = PackageContract.PackageInstallerEntry.SOLUTION_ID + " = ?";
        String[] selectionArgs = {solutionId};
        return context.getContentResolver().delete(PackageContract.INSTALLERS_CONTENT_URI, selection, selectionArgs) > 0;
    }

    public static void updateInstallerState(long rowId, PackageInstallerState state, Error error) {
        Context context = PackageManagerApplication.getAppContext();
        Uri installerUri = ContentUris.withAppendedId(PackageContract.INSTALLERS_CONTENT_URI, rowId);
        Log.d(TAG, "Updating installer (rowId=" + rowId + ") state to " + state + ", error=" + error);

        ContentValues updateValues = new ContentValues();
        updateValues.put(PackageContract.PackageInstallerEntry.STATE, state.name());
        if (error != null) {
            updateValues.put(PackageContract.PackageInstallerEntry.ERROR, JsonHelper.toJson(error));
        }
        updateValues.put(PackageContract.PackageInstallerEntry.LAST_UPDATED, new Date().getTime());
        int updated = context.getContentResolver().update(installerUri, updateValues, null, null);
        Log.d(TAG, "Installer state update result: " + updated);
    }

    // ─── Providers Table ─────────────────────────────────────────────────────

    public static void upsertProvider(String agentId, ContentValues cv) {
        Context context = PackageManagerApplication.getAppContext();
        String querySelection = PackageContract.PackageProviderEntry.AGENT_ID + " = ?";
        String[] querySelectionArgs = new String[]{agentId};

        Cursor providerCursor = context.getContentResolver().query(
                PackageContract.PROVIDERS_CONTENT_URI, PackageContract.ALL_PROJECTION_PROVIDER,
                querySelection, querySelectionArgs, null);
        try {
            if (providerCursor != null && providerCursor.moveToNext()) {
                int providerUpdated = context.getContentResolver().update(
                        PackageContract.PROVIDERS_CONTENT_URI, cv, querySelection, querySelectionArgs);
                Log.i(TAG, "provider is updated " + providerUpdated);
            } else {
                Uri providerInserted = context.getContentResolver().insert(
                        PackageContract.PROVIDERS_CONTENT_URI, cv);
                Log.i(TAG, "provider is successfully created at " + providerInserted);
            }
        } finally {
            if (providerCursor != null) providerCursor.close();
        }
    }

    public static int deleteProviders(String solutionId) {
        Context context = PackageManagerApplication.getAppContext();
        String querySelection = PackageContract.PackageProviderEntry.SOLUTION_ID + " = ?";
        String[] querySelectionArgs = new String[]{solutionId};

        int providerDeleted = context.getContentResolver().delete(
                PackageContract.PROVIDERS_CONTENT_URI, querySelection, querySelectionArgs);
        Log.d(TAG, "Package provider delete result = " + (providerDeleted > 0) +
                ", deleted items " + providerDeleted + ": " + solutionId);
        return providerDeleted;
    }

    // ─── Attestation Table ───────────────────────────────────────────────────

    public static void upsertAttestation(String solutionId, ContentValues cv) {
        Context context = PackageManagerApplication.getAppContext();
        Uri attestationUri = Uri.withAppendedPath(PackageContract.PACKAGES_ATTESTATION_CONTENT_URI, solutionId);

        Cursor cursor = context.getContentResolver().query(attestationUri, null, null, null, null);
        long currentTime = System.currentTimeMillis();
        cv.put(PackageContract.PackageAttestationEntry.MODIFY_DATE, currentTime);

        try {
            if (cursor != null && cursor.moveToNext()) {
                int updated = context.getContentResolver().update(attestationUri, cv, null, null);
                Log.i(TAG, "attestation is updated " + updated);
            } else {
                cv.put(PackageContract.PackageAttestationEntry.INSTALL_DATE, currentTime);
                Uri inserted = context.getContentResolver().insert(
                        PackageContract.PACKAGES_ATTESTATION_CONTENT_URI, cv);
                Log.i(TAG, "attestation is inserted " + inserted);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
