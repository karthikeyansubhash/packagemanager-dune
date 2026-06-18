package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions;

public class Solutions {

    Description description;
    InstallationDetails installationDetails;
    String resourceId;
    String solutionId;
    String state;
    AllowListType allowListType;

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public InstallationDetails getInstallationDetails() {
        return installationDetails;
    }

    public void setInstallationDetails(InstallationDetails installationDetails) {
        this.installationDetails = installationDetails;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public AllowListType getAllowListType() {
        return allowListType;
    }

    public void setAllowListType(AllowListType allowListType) {
        this.allowListType = allowListType;
    }
}
