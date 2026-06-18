/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServicesDiscoveryImpl implements ServicesDiscovery {
    @JsonProperty("version")
    private String version;
    @JsonProperty("services")
    private List<ServiceMetadataImpl> services;

    public ServicesDiscoveryImpl() {
    }

    public ServicesDiscoveryImpl(String version, List<ServiceMetadataImpl> services) {
        this.version = version;
        this.services = services;
    }

    @JsonProperty("version")
    @Override
    public String getVersion() {
        return this.version;
    }

    @JsonProperty("version")
    @Override
    public void setVersion(String version) {
        this.version = version;

    }

    @JsonProperty("services")
    @Override
    public List<ServiceMetadataImpl> getServices() {
        return this.services;
    }

    @JsonProperty("services")
    @Override
    public void setServices(List<ServiceMetadataImpl> services) {
        this.services = services;
    }

}
