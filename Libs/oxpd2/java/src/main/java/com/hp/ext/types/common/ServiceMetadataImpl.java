/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.ext.types.base.Link;

public class ServiceMetadataImpl implements ServiceMetadata {
    @JsonProperty("version")
    private String version;
    @JsonProperty("serviceGun")
    private String serviceGun;
    @JsonProperty("description")
    private String description;
    @JsonProperty("links")
    private List<Link> links;


    public ServiceMetadataImpl() {
    }

    public ServiceMetadataImpl(String version, String serviceGun, String description, List<Link> links) {
        this.version = version;
        this.serviceGun = serviceGun;
        this.description = description;
        this.links = links;
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

    @JsonProperty("serviceGun")
    @Override
    public String getServiceGun() {
        return this.serviceGun;
    }

    @JsonProperty("serviceGun")
    @Override
    public void setServiceGun(String serviceGun) {
        this.serviceGun = serviceGun;
    }

    @JsonProperty("description")
    @Override
    public String getDescription() {
        return this.description;
    }

    @JsonProperty("description")
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("links")
    @Override
    public List<Link> getLinks() {
        return this.links;
    }

    @JsonProperty("links")
    @Override
    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
