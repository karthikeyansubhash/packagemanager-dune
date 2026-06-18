package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data;

public class Install {
    String path;

    boolean forceInstall;

    String platformVersion;

    String solutionId;

    public String getPath() {
        return path;
    }

    public boolean isForceInstall() {
        return forceInstall;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setForceInstall(boolean forceInstall) {
        this.forceInstall = forceInstall;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }
}
