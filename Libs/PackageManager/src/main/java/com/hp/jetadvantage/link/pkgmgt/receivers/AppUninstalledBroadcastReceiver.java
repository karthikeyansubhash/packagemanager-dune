package com.hp.jetadvantage.link.pkgmgt.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.helper.EventReceiverManager;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;
import com.hp.jetadvantage.link.pkgmgt.model.PackageUninstallerState;
import com.hp.jetadvantage.link.pkgmgt.uninstaller.PostUninstaller;

public class AppUninstalledBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.TAG + "DeleteReceiver";
    private static final String ACTION_PACKAGE_UNINSTALL = "com.hp.packagemanager.intent.action.UNINSTALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            return;
        }
        final String action = intent.getAction();
        if (!ACTION_PACKAGE_UNINSTALL.equalsIgnoreCase(action)) {
            Log.d(TAG, "Ignoring unsupported uninstall action: " + action);
            return;
        }

        try {
            SecurityHelper.checkCallingPackageForLink(context);
        } catch (Throwable throwable) {
            Log.w(TAG, "Ignoring uninstall broadcast from unauthorized caller: " + throwable.getMessage());
            return;
        }

        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);
        String solutionId = intent.getStringExtra(PackageContract.Intent.EXTRA_SOLUTION_ID);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
        switch (status) {
            case PackageInstaller.STATUS_SUCCESS:
                Log.d(TAG, "Uninstall successful for package: " + packageName + ", solutionId: " + solutionId);
                sendUninstallMessage(PackageUninstallerState.usCompleted, solutionId, packageName, null);
                break;
            case PackageInstaller.STATUS_FAILURE:
                Log.d(TAG, "Uninstall failed for package: " + packageName);
                Log.d(TAG, "Failure reason: " + message);
            default:
                Log.d(TAG, "Uninstall status: " + status);
                sendUninstallMessage(PackageUninstallerState.usFailed, solutionId, packageName, message);
                break;
        }
    }

    private void sendUninstallMessage(PackageUninstallerState state, String solutionId, String packageName, String message) {
        PostUninstaller postUninstaller = new PostUninstaller(state, solutionId, packageName, message);
        postUninstaller.onStart();
    }
}
