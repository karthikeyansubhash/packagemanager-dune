package com.hp.jetadvantage.link.pkgmgt.hpkutil.application;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import javafx.concurrent.Task;

import java.util.UUID;

public class AppInfoGetServiceGUI extends javafx.concurrent.Service<Boolean> {
    private AppInfoGetService appInfoGetService;

    public AppInfoGetServiceGUI(String host, TaskInterface taskInterface) {
        appInfoGetService = new AppInfoGetService(host, taskInterface);
    }

    public AppInfoGetServiceGUI(String host, UUID uuid, TaskInterface taskInterface) {
        appInfoGetService = new AppInfoGetService(host, uuid, taskInterface);
    }

    public void execute() {
        appInfoGetService.execute();
    }

    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() {
                execute();
                return null;
            }
        };
    }
}
