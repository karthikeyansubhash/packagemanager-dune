package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import java.util.Date;
import java.util.List;

public class PackageInstaller {
    private String uuid;
    private String name;
    private String version;
    private PackageInstallerState state;
    private Date lastUpdated;
    private String error;
    private List<PackageInstallerActionLink> link;
    private String clientId;
    private String installSource;
    private String vendorName;

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

    public PackageInstallerState getState() {
        return state;
    }

    public void setState(PackageInstallerState state) {
        this.state = state;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<PackageInstallerActionLink> getLink() {
        return link;
    }

    public void setLink(List<PackageInstallerActionLink> link) {
        this.link = link;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getInstallSource() {
        return installSource;
    }

    public void setInstallSource(String installSource) {
        this.installSource = installSource;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @Override
    public String toString() {
        return "PackageInstaller{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", version='" + version + '\'' +
                ", state=" + state +
                ", lastUpdated=" + lastUpdated +
                ", error='" + error + '\'' +
                ", link=" + link +
                ", clientId='" + clientId + '\'' +
                ", installSource='" + installSource + '\'' +
                '}';
    }
}
