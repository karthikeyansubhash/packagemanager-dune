package com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Configuration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import javafx.concurrent.Task;

import java.util.UUID;

public class ConfigUpdateServiceGUI extends javafx.concurrent.Service<Boolean> {
    private ConfigUpdateService configUpdateService;

    public ConfigUpdateServiceGUI(String host, UUID uuid, Configuration configuration, TaskInterface taskInterface) {
        configUpdateService = new ConfigUpdateService(host, uuid, configuration, taskInterface);
    }

    public void execute() {
        configUpdateService.execute();
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
