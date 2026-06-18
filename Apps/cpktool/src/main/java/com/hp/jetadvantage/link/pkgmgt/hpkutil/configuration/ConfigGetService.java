package com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;

import java.util.UUID;

public class ConfigGetService {
    private String host;
    private UUID uuid;
    private TaskInterface taskInterface;

    public ConfigGetService(String host, UUID uuid, TaskInterface taskInterface) {
        this.host = host;
        this.uuid = uuid;
        this.taskInterface = taskInterface;
    }

    public void execute() {
        try {
            ConfigManager configManager = new ConfigManager(host, uuid);
            configManager.getConfiguration(taskInterface);
        } catch (Exception e) {
            taskInterface.onFailed(e);
        }
    }
}
