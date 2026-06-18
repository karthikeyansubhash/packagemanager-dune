package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import com.google.gson.annotations.SerializedName;

public enum PackageInstallerActionType {
    done,

    self,

    permissions,

    @SerializedName("accept-permissions")
    accept_permissions,

    @SerializedName("reject-permissions")
    reject_permissions
}
