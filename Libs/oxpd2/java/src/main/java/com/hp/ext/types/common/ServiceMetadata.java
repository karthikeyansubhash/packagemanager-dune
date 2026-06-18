/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import java.util.List;

import com.hp.ext.types.base.Link;

public interface ServiceMetadata {
    String getVersion();

    void setVersion(String version);

    String getServiceGun();

    void setServiceGun(String serviceGun);

    String getDescription();

    void setDescription(String description);

    List<Link> getLinks();

    void setLinks(List<Link> links);

}
