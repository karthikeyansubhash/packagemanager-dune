package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import java.util.UUID;

public class ProviderInfo {
    private UUID uuid;
    private String name;
    private String uri;
    private UUID parentUuid;
    private String parentName;
    private String metaData;
    private String installDate;
    private String functionType;
    private String description;
    private String param1;
    private String param2;
    private String param3;

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public UUID getParentUuid() {
        return parentUuid;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getInstallDate() {
        return installDate;
    }

    public String getFunctionType() {
        return functionType;
    }

    public String getDescription() {
        return description;
    }

    public String getParam1() {
        return param1;
    }

    public String getParam2() {
        return param2;
    }

    public String getParam3() {
        return param3;
    }

    // additional parameter
    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}