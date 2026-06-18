package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data;

public class InstallStatus {
    String status;

    String solutionId;

    String error;

    public InstallStatus(String status, String solutionId) {
        this.status = status;
        this.solutionId = solutionId;
    }

    public InstallStatus(String status, String solutionId, String error) {
        this.status = status;
        this.solutionId = solutionId;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public String getError() {
        return error;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "InstallStatus{" +
                "status='" + status + '\'' +
                ", solutionId='" + solutionId + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
