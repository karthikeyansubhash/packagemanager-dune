package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import java.util.UUID;

public class SubActivityInfo {
    UUID uuid;
    String platformId;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    @Override
    public String toString() {
        return platformId;
    }
}
