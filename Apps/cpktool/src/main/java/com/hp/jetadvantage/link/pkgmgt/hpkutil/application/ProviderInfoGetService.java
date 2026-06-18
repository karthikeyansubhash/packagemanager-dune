package com.hp.jetadvantage.link.pkgmgt.hpkutil.application;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;

public class ProviderInfoGetService{
    private String host;
    private TaskInterface taskInterface;

    public ProviderInfoGetService(String host, TaskInterface taskInterface) {
        this.host = host;
        this.taskInterface = taskInterface;
    }

    public void execute() {
        try {
            ProviderInfoManager providerInfoManager = new ProviderInfoManager(host);
            providerInfoManager.getProviderList(taskInterface);
        } catch (Exception e) {
            taskInterface.onFailed(e);
        }
    }
}
