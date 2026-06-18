package com.hp.jetadvantage.link.pkgmgt.model;

import java.util.List;

/**
 * Container for built-in apps JSON.
 */
public class BuiltinAppsContainer {
    private String version;
    private List<BuiltinApp> builtinApps;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<BuiltinApp> getBuiltinApps() {
        return builtinApps;
    }

    public void setBuiltinApps(List<BuiltinApp> builtinApps) {
        this.builtinApps = builtinApps;
    }
}

