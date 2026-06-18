package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;

public class TaskStatus {
    PackageInstallerState state;
    String cause;

    public TaskStatus(PackageInstallerState state, String cause){
        this.state = state;
        this.cause = cause;
    }

    public PackageInstallerState getState() {
        return state;
    }

    public String getCause() {
        return cause;
    }
}
