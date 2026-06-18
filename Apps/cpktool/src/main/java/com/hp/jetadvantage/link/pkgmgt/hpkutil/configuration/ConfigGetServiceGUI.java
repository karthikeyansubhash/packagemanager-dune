package com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import javafx.concurrent.Task;

import java.util.UUID;

public class ConfigGetServiceGUI extends javafx.concurrent.Service<Boolean> {
    private ConfigGetService configGetService;

    public ConfigGetServiceGUI(String host, UUID uuid, TaskInterface taskInterface) {
        configGetService = new ConfigGetService(host, uuid, taskInterface);
    }

    public void execute() {
        configGetService.execute();
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
