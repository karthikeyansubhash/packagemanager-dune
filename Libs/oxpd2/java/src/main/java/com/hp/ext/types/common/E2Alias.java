/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import com.fasterxml.jackson.annotation.JsonValue;

public class E2Alias<TAliasOf> extends E2Type{

    protected TAliasOf value;

    public Class<?> getAliasOf() {
        return value.getClass();
    }

    @JsonValue()
    public TAliasOf getValue()
    {
        return value;
    }

    protected E2Alias(TAliasOf value)
    {
        this.value = value;
    }
}
