package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data;

public class UninstallStatus {
    String status;

    String solutionId;

    String error;

    public UninstallStatus() {

    }

    public UninstallStatus(String status, String solutionId, String error) {
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
}
