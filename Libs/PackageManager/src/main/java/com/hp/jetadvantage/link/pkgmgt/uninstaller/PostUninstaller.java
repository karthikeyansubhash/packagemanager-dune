package com.hp.jetadvantage.link.pkgmgt.uninstaller;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;
import com.hp.jetadvantage.link.pkgmgt.controller.MessageController;
import com.hp.jetadvantage.link.pkgmgt.helper.DatabaseHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.EventReceiverManager;
import com.hp.jetadvantage.link.pkgmgt.helper.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.model.PackageUninstallerState;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.AppManagement;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.Details;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.PlatformMessage;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.UninstallStatus;
import com.hp.jetadvantage.link.pkgmgt.utils.Utils;

import java.net.HttpURLConnection;

public class PostUninstaller extends MessageController {
    private static final String TAG = Constants.TAG + "PostUninstaller";

    private PackageUninstallerState state;
    private final String solutionId;
    private final String packageName;
    private String message;

    public PostUninstaller(PackageUninstallerState state, String solutionId, String packageName, String message) {
        this.state = state;
        this.solutionId = solutionId;
        this.packageName = packageName;
        this.message = message;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onServiceConnected() {
        sendUninstallMessage(onPackageUninstalled());
    }

    private void sendUninstallMessage(UninstallStatus uninstallStatus) {
        int responseCode = 0;
        PlatformMessage message = new PlatformMessage();
        AppManagement appManagement = new AppManagement();
        Details details = new Details();

        if (PackageUninstallerState.usCompleted.equals(state)) {
            responseCode = HttpURLConnection.HTTP_OK;
        } else if (PackageUninstallerState.usInProgress.equals(state)) {
            responseCode = HttpURLConnection.HTTP_ACCEPTED;
        } else if (PackageUninstallerState.usFailed.equals(state)) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        }
        details.setUninstallStatus(uninstallStatus);
        appManagement.setDetails(details);
        message.setAppManagement(appManagement);
        sendMessage(responseCode, JsonHelper.toJson(message, false));
    }

    private UninstallStatus onPackageUninstalled() {
        if (state == PackageUninstallerState.usCompleted) {
            Log.d(TAG, "Uninstall complete message received: installer=" + solutionId + ", pkgName=" + packageName);

            try {
                finishUninstallation(solutionId, packageName);
            } catch (Exception e) {
                // do nothing - error state already saved to installer
                Log.e(TAG, "Error while processing post-uninstallation:" + e.getMessage(), e);
                state = PackageUninstallerState.usFailed;
                message = "Error while processing post-uninstallation:" + e.getMessage();
            }
        }
        UninstallStatus uninstallStatus = new UninstallStatus();
        uninstallStatus.setStatus(state.name());
        uninstallStatus.setSolutionId(solutionId);
        uninstallStatus.setError(message);

        return uninstallStatus;
    }

    public void finishUninstallation(String solutionId, String pkgName) {
        Context context = PackageManagerApplication.getAppContext();
        String applicationId = DatabaseHelper.queryMainApplicationId(solutionId);

        // 1. remove from DB (installer)
        Log.e(TAG, "remove from DB (app info)");
        if (!DatabaseHelper.deleteInstaller(solutionId)) {
            Log.e(TAG, "Package failed to be removed from installer database");
        }

        // 2. remove from DB (packages)
        if (!DatabaseHelper.deletePackageRecord(solutionId)) {
            Log.e(TAG, "Package failed to be removed from packages database");
        }

        // 3. remove from DB (providers)
        int deletedCount = DatabaseHelper.deleteProviders(solutionId);
        if (deletedCount > 0) {
            Log.d(TAG, "Package providers are deleted items " + deletedCount + ": " + solutionId);

            // 4. Unregister event receivers (DUNE-314377)
            if (!TextUtils.isEmpty(pkgName)) {
                EventReceiverManager eventReceiverManager = new EventReceiverManager(context);
                boolean unregistered = eventReceiverManager.unregisterEventReceivers(pkgName);
                Log.i(TAG, "Event receiver cleanup result=" + unregistered
                        + " package=" + pkgName);
            }

            // 5. notify finish installation
            String action = PackageContract.Intent.ACTION_PACKAGE_UNINSTALLED;

            Bundle extras = new Bundle(1);
            extras.putString(PackageContract.Intent.EXTRA_APPLICATION_AGENT_ID, applicationId);
            extras.putString(PackageContract.Intent.EXTRA_SOLUTION_ID, solutionId);
            extras.putString(PackageContract.Intent.EXTRA_CLIENT_ID, "ciJamc");
            extras.putString(PackageContract.Intent.EXTRA_INSTALL_SOURCE, "isStandardRepository");
            if (!TextUtils.isEmpty(pkgName)) {
                extras.putString(PackageContract.Intent.EXTRA_PACKAGE, pkgName);
            }

            Utils.sendPackageBroadcast(context, action,
                    extras, PackageContract.Permission.PACKAGE_LIFECYCLE_EVENTS);
        }
    }
}
