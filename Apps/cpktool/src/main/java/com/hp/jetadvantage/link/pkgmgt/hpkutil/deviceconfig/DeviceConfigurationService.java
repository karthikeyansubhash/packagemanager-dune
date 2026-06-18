package com.hp.jetadvantage.link.pkgmgt.hpkutil.deviceconfig;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.InstallListener;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceConfiguration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import javafx.concurrent.Task;

public class DeviceConfigurationService extends javafx.concurrent.Service<Boolean> {
    private String host;
    private String username;
    private String password;
    private TaskInterface taskInterface;

    public DeviceConfigurationService(TaskInterface taskInterface) {
        this.taskInterface = taskInterface;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void execute() {
        DeviceConfigurationManager manager = new DeviceConfigurationManager(host);
        manager.setUsername(username);
        manager.setUserPassword(password);
        manager.getDeviceConfiguration(deviceConfigurationListener);
    }

    private DeviceConfigurationListener deviceConfigurationListener = new DeviceConfigurationListener() {
        @Override
        public void status(PackageInstallerState status, String cause) {
            TaskStatus taskStatus = new TaskStatus(status, cause);
            taskInterface.updateMessage(taskStatus);
            if (status.equals(PackageInstallerState.psFailed)) {
                this.finished();
            }
        }

        @Override
        public void complete(DeviceConfiguration deviceConfiguration) {
            taskInterface.onSucceed(deviceConfiguration);
        }

        @Override
        public void finished() {
            taskInterface.onSucceed(null);
        }
    };

    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                execute();
                return null;
            }
        };
    }
}
