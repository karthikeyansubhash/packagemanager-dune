package com.hp.jetadvantage.link.pkgmgt.model;

/**
 * Model representing a single built-in app entry.
 */
public class BuiltinApp {
    private String agentId;
    private String name;
    private String description;
    private String packageName;
    private String activityName;

    public BuiltinApp() {
        // Default constructor for JSON deserialization
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}

