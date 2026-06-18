package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;

import java.util.UUID;

public class UninstallService {
    private String host;
    private UUID uuid;
    private String username;
    private String password;
    private TaskInterface taskInterface;

    public UninstallService(TaskInterface taskInterface) {
        this.taskInterface = taskInterface;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void execute() {
        if (DeviceMode.OXPD.equals(Constants.DEFAULT_DEVICE_MODE)) {
            HPKOXPdUninstaller uninstaller = new HPKOXPdUninstaller(host);
            uninstaller.setUsername(username);
            uninstaller.setUserPassword(password);
            uninstaller.uninstall(uuid.toString(), uninstallListener);
        } else {
            HPKUninstaller uninstaller = new HPKUninstaller(host);
            uninstaller.setUsername(username);
            uninstaller.setUserPassword(password);
            uninstaller.uninstall(uuid.toString(), uninstallListener);
        }
    }

    private InstallListener uninstallListener = new InstallListener() {
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
