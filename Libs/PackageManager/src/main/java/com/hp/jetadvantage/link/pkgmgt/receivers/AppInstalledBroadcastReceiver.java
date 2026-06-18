package com.hp.jetadvantage.link.pkgmgt.receivers;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.helper.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;
import com.hp.jetadvantage.link.pkgmgt.installer.PostInstaller;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions.Solutions;

/**
 * BroadcastReceiver for handling package installation events in HP system apps.
 * - Listens for DUNE_PACKAGE_ADDED broadcasts. <- It's coming from PreInstaller.
 * - Performs security checks on the calling package.
 * - Extracts installation status, package name, and solution info from the broadcast.
 * - Initiates post-installation processing via PostInstaller.
 */
public class AppInstalledBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = Constants.TAG + "InstalledReceiver";
    public static final String ACTION_PACKAGE_ADDED = "com.hp.packagemanager.intent.action.DUNE_PACKAGE_ADDED";
    private final String SYSTEM_SVC_FLAG = "PKGMGT_FLAG";

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (context != null && intent != null && intent.getAction() != null) {

                SecurityHelper.checkCallingPackageForLink(context); //throws SecurityException
                if (Application.getProcessName().contains(":PackageManager")) {
                    if (intent.getExtras() != null && ACTION_PACKAGE_ADDED.equalsIgnoreCase(intent.getAction())) {
                        String sysFlag = "";
                        try {
                            sysFlag = System.getProperty(SYSTEM_SVC_FLAG);
                        } catch (Exception ignored) {
                        }

                        Bundle extras = intent.getExtras();
                        boolean isDuneInstaller = extras.getBoolean(Constants.DUNE_INSTALLER);

                        if (isDuneInstaller) {
                            int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
                            String pkgName = extras.getString(PackageInstaller.EXTRA_PACKAGE_NAME);
                            String solutionsMsg = extras.getString(Constants.SOLUTIONS);
                            String filePath = extras.getString(Constants.FILE_PATH);
                            Solutions solutions = JsonHelper.fromJson(solutionsMsg, Solutions.class);

                            PostInstaller postInstaller = new PostInstaller(status, solutions, pkgName, message, filePath);
                            postInstaller.onStart();

                        } else {
                            Log.e(TAG, "sysFlag: " + sysFlag + " , isDuneInstaller: " + isDuneInstaller);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Invalid access");
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Received event 2, but no permission " + throwable.getMessage());
            PostInstaller postInstaller = new PostInstaller(PackageInstaller.STATUS_FAILURE, "test error");
            postInstaller.onStart();
        }
    }
}
