package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import javafx.concurrent.Task;

import java.io.File;

public class InstallServiceGUI extends javafx.concurrent.Service<Boolean> {
    private InstallService installService;

    public InstallServiceGUI(TaskInterface taskInterface) {
        installService = new InstallService(taskInterface);
    }

    public void setHost(String host) {
        installService.setHost(host);
    }

    public void setCurrentFile(File currentFile) {
        installService.setCurrentFile(currentFile);
    }

    public void setAccount(String username, String password) {
        installService.setAccount(username, password);
    }

    public void setOptions(Boolean forceInstall) {
        installService.setOptions(forceInstall);
    }

    public void execute() {
        installService.execute();
    }

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
