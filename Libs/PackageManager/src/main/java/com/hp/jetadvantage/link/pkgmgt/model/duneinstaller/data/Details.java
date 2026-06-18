package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data;

public class Details {
    Install install;
    InstallStatus installStatus;
    Uninstall uninstall;
    UninstallStatus uninstallStatus;
    UpdateConfig updateConfig;

    public UpdateConfig getUpdateConfig() {
        return updateConfig;
    }

    public void setUpdateConfig(UpdateConfig updateConfig) {
        this.updateConfig = updateConfig;
    }

    public Install getInstall() {
        return install;
    }

    public void setInstall(Install install) {
        this.install = install;
    }

    public InstallStatus getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(InstallStatus installStatus) {
        this.installStatus = installStatus;
    }

    public Uninstall getUninstall() {
        return uninstall;
    }

    public void setUninstall(Uninstall uninstall) {
        this.uninstall = uninstall;
    }

    public UninstallStatus getUninstallStatus() {
        return uninstallStatus;
    }

    public void setUninstallStatus(UninstallStatus uninstallStatus) {
        this.uninstallStatus = uninstallStatus;
    }
}
