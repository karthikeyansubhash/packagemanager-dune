package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;

import java.io.File;

public class InstallService {
    private String host;
    private File currentFile;
    private String username;
    private String password;
    private Boolean forceInstall = Boolean.FALSE;
    private TaskInterface taskInterface;

    public InstallService(TaskInterface taskInterface) {
        this.taskInterface = taskInterface;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public void setAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setOptions(Boolean forceInstall) {
        this.forceInstall = forceInstall;
    }

    public void execute() {
        if (DeviceMode.OXPD.equals(Constants.DEFAULT_DEVICE_MODE)) {
            HPKOXPdInstaller installer = new HPKOXPdInstaller(host);
            installer.setUsername(username);
            installer.setUserPassword(password);
            installer.install(currentFile, installListener);
        } else {
            HPKInstaller installer = new HPKInstaller(host);
            installer.setUsername(username);
            installer.setUserPassword(password);
            installer.install(currentFile, forceInstall, installListener);
        }
    }

    private InstallListener installListener = new InstallListener() {
        @Override
        public void status(PackageInstallerState status, String cause) {
            TaskStatus taskStatus = new TaskStatus(status, cause);
            taskInterface.updateMessage(taskStatus);
            if (status.equals(PackageInstallerState.psFailed)) {
                this.finished();
            }
        }

        @Override
        public void finished() {
            taskInterface.onSucceed(null);
        }
    };
}
