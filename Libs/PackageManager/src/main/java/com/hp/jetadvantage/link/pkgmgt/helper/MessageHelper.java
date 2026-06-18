package com.hp.jetadvantage.link.pkgmgt.helper;

import android.content.Context;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.installer.PreInstaller;
import com.hp.jetadvantage.link.pkgmgt.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.model.PackageUninstallerState;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.AppManagement;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.Details;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.InstallStatus;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.PlatformMessage;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.UninstallStatus;
import com.hp.jetadvantage.link.pkgmgt.uninstaller.PreUninstaller;

public class MessageHelper {
    private static final String TAG = Constants.TAG + "MessageHelper";

    public static final String WHO_AM_I = "appManagement";

    public void parseMessage(Context context, String data) throws Exception {
        Log.i(TAG, "input data: " + data);

        PlatformMessage platformMessage = parsePlatformMessage(data);
        AppManagement appManagement = platformMessage.getAppManagement();

        if (appManagement.getDetails().getInstall() != null) {
            try {
                PreInstaller preInstaller = new PreInstaller(appManagement);
                preInstaller.install(context);
            } catch (Exception e) {
                InstallStatus installStatus = new InstallStatus(PackageInstallerState.isFailed.name(),
                        appManagement.getDetails().getInstall().getSolutionId(), e.getMessage());
                throw new Exception(JsonHelper.toJson(composeReturnMessage(installStatus)));
            }
        } else if (appManagement.getDetails().getUninstall() != null) {
            String solutionId = appManagement.getDetails().getUninstall().getSolutionId();
            try {
                PreUninstaller preUninstaller = new PreUninstaller(solutionId);
                preUninstaller.uninstall(context);
            } catch (Exception e) {
                UninstallStatus uninstallStatus = new UninstallStatus(PackageUninstallerState.usFailed.name(), solutionId, e.getMessage());
                throw new Exception(JsonHelper.toJson(composeReturnMessage(uninstallStatus)));
            }
        } else {
            Log.e(TAG, "DuneMessageHelper:parseMessage: Unsupported type!! ");
        }
    }

    private PlatformMessage parsePlatformMessage(String data) {
        return JsonHelper.fromJson(data, PlatformMessage.class);
    }

    private PlatformMessage composeReturnMessage(InstallStatus installStatus) {
        PlatformMessage returnMessage = new PlatformMessage();
        AppManagement appManagement = new AppManagement();
        Details details = new Details();
        details.setInstallStatus(installStatus);
        appManagement.setDetails(details);
        returnMessage.setAppManagement(appManagement);
        return returnMessage;
    }

    private PlatformMessage composeReturnMessage(UninstallStatus uninstallStatus) {
        PlatformMessage returnMessage = new PlatformMessage();
        AppManagement appManagement = new AppManagement();
        Details details = new Details();
        details.setUninstallStatus(uninstallStatus);
        appManagement.setDetails(details);
        returnMessage.setAppManagement(appManagement);
        return returnMessage;
    }
}
