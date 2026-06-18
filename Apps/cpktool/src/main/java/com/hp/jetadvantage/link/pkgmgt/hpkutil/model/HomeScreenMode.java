package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public class HomeScreenMode {
    boolean enableHomeScreenMode;
    boolean configOnInstall;

    public boolean isEnableHomeScreenMode() {
        return enableHomeScreenMode;
    }

    public void setEnableHomeScreenMode(boolean enableHomeScreenMode) {
        this.enableHomeScreenMode = enableHomeScreenMode;
    }

    public boolean isConfigOnInstall() {
        return configOnInstall;
    }

    public void setConfigOnInstall(boolean configOnInstall) {
        this.configOnInstall = configOnInstall;
    }
}
