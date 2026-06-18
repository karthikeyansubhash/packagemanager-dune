package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.UUID;

public class UninstallServiceGUI extends Service<Boolean> {
    private UninstallService uninstallService;

    public UninstallServiceGUI(TaskInterface taskInterface) {
        uninstallService = new UninstallService(taskInterface);
    }

    public void setHost(String host) {
        uninstallService.setHost(host);
    }

    public void setUuid(UUID uuid) {
        uninstallService.setUuid(uuid);
    }

    public void setAccount(String username, String password) {
        uninstallService.setAccount(username, password);
    }

    public void execute() {
        uninstallService.execute();
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