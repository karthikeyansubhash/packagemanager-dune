/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import com.fasterxml.jackson.annotation.*;

public class E2Type {

    public E2Type() {
        versionAdded = "1.0";
        majorVersion = 1;
        typeGUN = "";
        typeName = "";
    }

    protected String typeName;
    protected String typeGUN;
    protected String versionAdded;
    protected short majorVersion;

    @JsonProperty("$e2_TypeName")
    @JsonIgnore()
    public String getTypeName() {
        return typeName;
    }

    @JsonProperty("$e2_TypeGUN")
    @JsonIgnore()
    public String getTypeGUN() {
        return typeGUN;
    }

    @JsonProperty("$e2_MajorVersion")
    @JsonIgnore()
    public short getMajorVersion() {
        return majorVersion;
    }

    @JsonProperty("$e2_VersionAdded")
    @JsonIgnore()
    public String getVersionAdded() {
        return versionAdded;
    }
}
