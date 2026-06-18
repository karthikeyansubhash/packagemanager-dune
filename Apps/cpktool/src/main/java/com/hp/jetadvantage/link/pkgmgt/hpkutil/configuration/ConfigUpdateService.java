package com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Configuration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;

import java.util.UUID;

public class ConfigUpdateService {
    private String host;
    private UUID uuid;
    private Configuration configuration;
    private TaskInterface taskInterface;
    private Exception exception;

    public ConfigUpdateService(String host, UUID uuid, Configuration configuration, TaskInterface taskInterface) {
        this.host = host;
        this.uuid = uuid;
        this.configuration = configuration;
        this.taskInterface = taskInterface;
    }

    public void execute() {
        try {
            ConfigManager configManager = new ConfigManager(host, uuid);

            if (configuration.getSchema() == null) {
                Configuration original = configManager.getConfiguration(dummyTaskInterface());
                if (original == null && exception != null) {
                    taskInterface.onFailed(exception);
                    return;
                }
                configuration.setSchema(original.getSchema());
            }

            configManager.putConfiguration(configuration, taskInterface);
        } catch (Exception e) {
            taskInterface.onFailed(e);
        }
    }

    private TaskInterface dummyTaskInterface() {
        return new TaskInterface() {
            @Override
            public String updateMessage(TaskStatus msg) {
                return null;
            }

            @Override
            public void onSucceed(Object obj) {
            }

            @Override
            public void onFailed(Exception e) {
                exception = e;
            }
        };
    }
}
