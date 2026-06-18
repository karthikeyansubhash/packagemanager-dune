/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import java.util.List;

public interface ServicesDiscovery {
    String getVersion();

    void setVersion(String version);

    List<ServiceMetadataImpl> getServices();

    void setServices(List<ServiceMetadataImpl> services);
}
