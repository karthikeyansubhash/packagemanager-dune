package com.hp.jetadvantage.link.pkgmgt.installer;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hp.ext.types.solutionManager.SolutionState;
import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.R;
import com.hp.jetadvantage.link.pkgmgt.exception.OutOfStorageException;
import com.hp.jetadvantage.link.pkgmgt.exception.PacManException;
import com.hp.jetadvantage.link.pkgmgt.helper.DatabaseHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;
import com.hp.jetadvantage.link.pkgmgt.model.Error;
import com.hp.jetadvantage.link.pkgmgt.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.AppManagement;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.Install;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions.AllowListType;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions.Solutions;
import com.hp.jetadvantage.link.pkgmgt.receivers.AppInstalledBroadcastReceiver;
import com.hp.jetadvantage.link.pkgmgt.utils.CDM;
import com.hp.jetadvantage.link.pkgmgt.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class PreInstaller {

    private static final String TAG = Constants.TAG + "PreInstaller";
    private static final String INSTALL_PATH = "/data/workpath/Customer/solutions";

    private AppManagement appManagement;

    public PreInstaller(AppManagement appManagement) {
        this.appManagement = appManagement;
    }

    public void install(Context context) throws Exception {
        Install install = appManagement.getDetails().getInstall();

        /**
         * Validation check
         * 1. Platform version
         * 2. APK file exist
         * 3. Check package name
         * 4. Check enough space for install apk
         * 5. (After install) Request Extensibility API about current installation information
         */
        // 1. Platform version check
        if (!install.getPlatformVersion().isEmpty()) {
            SecurityHelper.isValidPlatformVersion(install.getPlatformVersion());
        } else {
            throw new PacManException(Error.Code.ec_invalid_request, context.getString(R.string.error_mandatory_params_missing) + "/ PlatformVersion is not valid", Error.Entity.install);
        }

        // 2. APK file exist and check package name
        // 2-1 check apk file
        String installPath = INSTALL_PATH + "/" + install.getSolutionId() + "/" + install.getPath();
        Log.i(TAG, "getPath: " + installPath);
        File apkFile = new File(installPath);
        File destFile = new File(context.getCacheDir().getAbsolutePath() + "/" + apkFile.getName());
        if (apkFile.exists()) {
            // APK file copy to local pacman area.
            // reason: when we get package name from apk file, file resource is not released after using. so unmount was failed after installation.
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            try (FileChannel source = new FileInputStream(apkFile).getChannel();
                 FileChannel destination = new FileOutputStream(destFile).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
        } else {
            throw new PacManException(Error.Code.ec_invalid_request, context.getString(R.string.error_mandatory_params_missing) + "/ apk file is null", Error.Entity.install);
        }

        // 2-2 check package name
        String packageName = getPackageName(context, destFile);
        Log.i(TAG, "filename: " + destFile.getName());

        // 3. Request REST API about current install information
        String solutionState = CDM.getSolutions(install.getSolutionId()).get();
        Solutions solutions = JsonHelper.fromJson(solutionState, Solutions.class);
        if (!SolutionState.SsInstalling.getValue().equals(solutions.getState())) {
            throw new PacManException(Error.Code.ec_invalid_request, context.getString(R.string.error_params_not_valid) + "/" + solutions.getState(), Error.Entity.install);
        }
        if (!solutions.getSolutionId().equals(install.getSolutionId())) {
            throw new PacManException(Error.Code.ec_invalid_request, context.getString(R.string.error_params_not_valid) + "/solutionId is different", Error.Entity.install);
        }

        // 4. Check certificate (Golden app)
        PackageInfo pInfo = context.getPackageManager().getPackageArchiveInfo(apkFile.getPath(), PackageManager.GET_SIGNING_CERTIFICATES);
        android.content.pm.Signature[] signatures = (pInfo != null && pInfo.signingInfo != null)
                ? pInfo.signingInfo.getApkContentsSigners()
                : null;
        if (SecurityHelper.isGoldListed(signatures, apkFile.getPath())) {
            Log.i(TAG, "isGoldListed: true");
            solutions.setAllowListType(AllowListType.GOLD);
        } else {
            Log.i(TAG, "isGoldListed: false");
        }

        // 5. Check enough space for install apk
        if (!Utils.isDataPartitionHasEnoughSpaceForFileSize(context, destFile.length())) {
            throw new OutOfStorageException();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "mHandler post: start");
                    ContentValues cv = DatabaseHelper.buildInstallerValues(solutions, packageName, PackageInstallerState.isInProgress, null);
                    DatabaseHelper.upsertInstaller(cv);
                    installApk(context, destFile, packageName, solutions);
                } catch (Exception e) {
                    Log.e(TAG, "pre-installation failed: " + e.getMessage(), e);
                } finally {
                    Log.i(TAG, "mHandler post: end");
                }
            }
        });
    }

    private void installApk(Context context, File apkFile, String packageName, Solutions solutions) {
        Log.i(TAG, "install request installApk");

        PackageManager packageManger = context.getPackageManager();
        PackageInstaller packageInstaller = packageManger.getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);

        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            int sessionId = packageInstaller.createSession(params);
            session = packageInstaller.openSession(sessionId);
            out = session.openWrite(packageName, 0, apkFile.length());
            //readTo(Uri.fromFile(apkFile), out); //read the apk content and write it to out
            byte[] buffer = new byte[65536];
            in = new FileInputStream(apkFile);
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            in.close();
            out.close();

            Intent intentEv = new Intent(AppInstalledBroadcastReceiver.ACTION_PACKAGE_ADDED); //Intent.ACTION_PACKAGE_ADDED);
            intentEv.setPackage(com.hp.jetadvantage.link.pkgmgt.PackageManager.SERVICES_PACKAGE);
            intentEv.setData(Uri.parse(Constants.PACKAGE_TAG + packageName));
            intentEv.putExtra(Constants.DUNE_INSTALLER, true);
            intentEv.putExtra(Constants.FILE_PATH, apkFile.getPath());
            intentEv.putExtra(Constants.SOLUTIONS, JsonHelper.toJson(solutions));
            intentEv.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            PendingIntent intent = PendingIntent.getBroadcast(context,
                    sessionId,
                    intentEv,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            session.commit(intent.getIntentSender());
            Log.i(TAG, "install request sent: " + sessionId);
        } catch (Throwable throwable) {
            // send error message
            Log.e(TAG, "pre-installation failed: " + throwable.getMessage(), throwable);
            PostInstaller postInstaller = new PostInstaller(PackageInstaller.STATUS_FAILURE, throwable.getMessage());
            postInstaller.onStart();
        } finally {
            if (session != null) {
                session.close();
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String getPackageName(Context context, File installFile) {
        PackageInfo info = context.getPackageManager().getPackageArchiveInfo(installFile.getAbsolutePath(), 0);
        if (info != null) {
            return info.packageName;
        }
        return null;
    }
}
