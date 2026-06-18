package com.hp.jetadvantage.link.pkgmgt.model;

import java.util.Date;

public class Package {
    private String uuid;
    private String name;
    private String version;
    private String packageName;
    private String platformType;
    private Date installDate;
    private String metaData;
    private String vendorName;
    private String description;
    private String icon;
    private String scActivityName;
    private String scParentUuid;
    private String scFunctionType;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(Date installDate) {
        this.installDate = installDate;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendor) {
        this.vendorName = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getScActivityName() {
        return scActivityName;
    }

    public void setScActivityName(String scActivityName) {
        this.scActivityName = scActivityName;
    }

    public String getScParentUuid() {
        return scParentUuid;
    }

    public void setScParentUuid(String scParentUuid) {
        this.scParentUuid = scParentUuid;
    }

    public String getScFunctionType() {
        return scFunctionType;
    }

    public void setScFunctionType(String scFunctionType) {
        this.scFunctionType = scFunctionType;
    }

    @Override
    public String toString() {
        return "Package{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", packageName='" + packageName + '\'' +
                ", platformType='" + platformType + '\'' +
                ", installDate=" + installDate +
                ", metaData='" + metaData + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", scActivityName='" + scActivityName + '\'' +
                ", scParentUuid='" + scParentUuid + '\'' +
                ", scFunctionType='" + scFunctionType + '\'' +
                '}';
    }
}
