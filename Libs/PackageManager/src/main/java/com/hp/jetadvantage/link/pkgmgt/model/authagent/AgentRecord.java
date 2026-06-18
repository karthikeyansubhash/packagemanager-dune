package com.hp.jetadvantage.link.pkgmgt.model.authagent;

import com.hp.jetadvantage.link.pkgmgt.lib.LocalizedString;

public class AgentRecord {
    String id;
    String name;
    String agentId;
    LocalizedString[] localizedName;
    LocalizedString[] localizedDescription;
    boolean enablePrePromptCheck;
    String userPromptTarget;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public LocalizedString[] getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(LocalizedString[] localizedName) {
        this.localizedName = localizedName;
    }

    public LocalizedString[] getLocalizedDescription() {
        return localizedDescription;
    }

    public void setLocalizedDescription(LocalizedString[] localizedDescription) {
        this.localizedDescription = localizedDescription;
    }

    public boolean isEnablePrePromptCheck() {
        return enablePrePromptCheck;
    }

    public void setEnablePrePromptCheck(boolean enablePrePromptCheck) {
        this.enablePrePromptCheck = enablePrePromptCheck;
    }

    public String getUserPromptTarget() {
        return userPromptTarget;
    }

    public void setUserPromptTarget(String userPromptTarget) {
        this.userPromptTarget = userPromptTarget;
    }
}
