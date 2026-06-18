package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public class DeviceConfiguration {
    private String version;
    private String currentLinkPlatformEnabled;
    private String requestedLinkPlatformEnabled;
    private String currentLinkDebugBridgeEnabled;
    private String linkPlatformVersion;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCurrentLinkPlatformEnabled() {
        return currentLinkPlatformEnabled;
    }

    public void setCurrentLinkPlatformEnabled(String currentLinkPlatformEnabled) {
        this.currentLinkPlatformEnabled = currentLinkPlatformEnabled;
    }

    public String getRequestedLinkPlatformEnabled() {
        return requestedLinkPlatformEnabled;
    }

    public void setRequestedLinkPlatformEnabled(String requestedLinkPlatformEnabled) {
        this.requestedLinkPlatformEnabled = requestedLinkPlatformEnabled;
    }

    public String getCurrentLinkDebugBridgeEnabled() {
        return currentLinkDebugBridgeEnabled;
    }

    public void setCurrentLinkDebugBridgeEnabled(String currentLinkDebugBridgeEnabled) {
        this.currentLinkDebugBridgeEnabled = currentLinkDebugBridgeEnabled;
    }

    public String getLinkPlatformVersion() {
        return linkPlatformVersion;
    }

    public void setLinkPlatformVersion(String linkPlatformVersion) {
        this.linkPlatformVersion = linkPlatformVersion;
    }
}