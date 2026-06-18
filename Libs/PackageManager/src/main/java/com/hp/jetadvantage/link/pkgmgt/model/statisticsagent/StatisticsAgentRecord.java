package com.hp.jetadvantage.link.pkgmgt.model.statisticsagent;

public class StatisticsAgentRecord {
    String agentId;
    String name;
    boolean criticalSolution;
    Explicit localizedName;
    Explicit localizedDescription;
    boolean enableStatisticsCheck;
    NotificationTarget notificationTarget;

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

    public boolean isCriticalSolution() {
        return criticalSolution;
    }

    public void setCriticalSolution(boolean criticalSolution) {
        this.criticalSolution = criticalSolution;
    }

    public Explicit getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(Explicit localizedName) {
        this.localizedName = localizedName;
    }

    public Explicit getLocalizedDescription() {
        return localizedDescription;
    }

    public void setLocalizedDescription(Explicit localizedDescription) {
        this.localizedDescription = localizedDescription;
    }

    public boolean isEnableStatisticsCheck() {
        return enableStatisticsCheck;
    }

    public void setEnableStatisticsCheck(boolean enableStatisticsCheck) {
        this.enableStatisticsCheck = enableStatisticsCheck;
    }

    public NotificationTarget getNotificationTarget() {
        return notificationTarget;
    }

    public void setNotificationTarget(NotificationTarget notificationTarget) {
        this.notificationTarget = notificationTarget;
    }
}
