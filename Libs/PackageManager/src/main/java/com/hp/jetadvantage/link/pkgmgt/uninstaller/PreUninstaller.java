package com.hp.jetadvantage.link.pkgmgt.uninstaller;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.helper.DatabaseHelper;
import com.hp.jetadvantage.link.pkgmgt.model.PackageUninstallerState;
import com.hp.jetadvantage.link.pkgmgt.receivers.AppUninstalledBroadcastReceiver;

import java.util.ArrayList;

public class PreUninstaller {

    private static final String TAG = Constants.TAG + "PreUninstaller";
    private final String solutionId;

    public PreUninstaller(String solutionId) {
        this.solutionId = solutionId;
    }

    public void uninstall(Context context) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                ArrayList<ContentValues> cvList = DatabaseHelper.queryInstallerSeedFromPackages(solutionId);
                if (cvList != null && !cvList.isEmpty()) {
                    // for uninstall we use first content values
                    ContentValues cv = cvList.get(0);

                    cv.put(PackageContract.PackageInstallerEntry.STATE, PackageUninstallerState.usInProgress.name());
                    String selection = PackageContract.PackageInstallerEntry.SOLUTION_ID + " = ?";
                    String[] selectionArgs = new String[]{ solutionId };
                    context.getContentResolver().update(PackageContract.INSTALLERS_CONTENT_URI, cv, selection, selectionArgs);

                    String packageName = cv.getAsString(PackageContract.PackageInstallerEntry.PACKAGE_NAME);
                    if (isApplicationInstalled(context, packageName)) {
                        deletePackage(context, packageName);
                    } else {
                        Log.w(TAG, "app is not installed, remove package data");
                        sendUninstallMessage(PackageUninstallerState.usCompleted, packageName, null);
                    }
                } else {
                    Log.e(TAG, "Package is not exist in database");
                    sendUninstallMessage(PackageUninstallerState.usCompleted, null, null);
                }
            } catch (Exception e) {
                // do nothing - error state already saved to installer
                Log.e(TAG, "Error while processing pre-uninstallation:" + e.getMessage(), e);
                sendUninstallMessage(PackageUninstallerState.usFailed, null, "Error while processing pre-uninstallation:" + e.getMessage());
            }
        });
    }

    private void sendUninstallMessage(PackageUninstallerState state, String packageName, String message) {
        PostUninstaller postUninstaller = new PostUninstaller(state, solutionId, packageName, message);
        postUninstaller.onStart();
    }

    private boolean isApplicationInstalled(final Context context, final String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return false;
    }

    private void deletePackage(Context context, String packageName) {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        Intent intent = new Intent(context, AppUninstalledBroadcastReceiver.class);
        intent.setAction(PackageContract.Intent.UNINSTALL_PACKAGE);
        intent.putExtra(PackageContract.Intent.EXTRA_SOLUTION_ID, solutionId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        IntentSender intentSender = pendingIntent.getIntentSender();
        packageInstaller.uninstall(packageName, intentSender);
    }
}
