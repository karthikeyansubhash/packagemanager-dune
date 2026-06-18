package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

public class HomeScreenData {
    public String appId;
    public String intentUri;
    public String module;
    public boolean nativeHomeScreenFallbackEnabled;
    public String title;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIntentUri() {
        return intentUri;
    }

    public void setIntentUri(String intentUri) {
        this.intentUri = intentUri;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isNativeHomeScreenFallbackEnabled() {
        return nativeHomeScreenFallbackEnabled;
    }

    public void setNativeHomeScreenFallbackEnabled(boolean nativeHomeScreenFallbackEnabled) {
        this.nativeHomeScreenFallbackEnabled = nativeHomeScreenFallbackEnabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
