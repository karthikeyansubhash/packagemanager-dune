package com.hp.jetadvantage.link.pkgmgt.hpkutil.application;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;

import java.util.UUID;

public class AppInfoGetService {
    private String host;
    private UUID uuid;
    private TaskInterface taskInterface;

    public AppInfoGetService(String host, TaskInterface taskInterface) {
        this.host = host;
        this.uuid = null;
        this.taskInterface = taskInterface;
    }

    public AppInfoGetService(String host, UUID uuid, TaskInterface taskInterface) {
        this.host = host;
        this.uuid = uuid;
        this.taskInterface = taskInterface;
    }

    public void execute() {
        try {
            AppInfoManager appInfoManager = new AppInfoManager(host, uuid);

            if (uuid == null) {
                appInfoManager.getAppInfoList(taskInterface);
            } else {
                appInfoManager.getDetailAppInfo(taskInterface);
            }
        } catch (Exception e) {
            if (DeviceMode.OXPD.equals(Constants.DEFAULT_DEVICE_MODE)) {
                executeOXPd();
            } else {
                taskInterface.onFailed(e);
            }
        }
    }

    private void executeOXPd() {
        try {
            AppInfoManager appInfoManager = new AppInfoManager(host, uuid);
            appInfoManager.getOXPdAppInfoList(taskInterface);
        } catch (Exception e) {
            taskInterface.onFailed(e);
        }
    }
}
