/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

public class TypedObject {

    private String typeGUN = "";

    public String getTypeGUN() {
        return typeGUN;
    }

    public void setTypeGUN(String value) {
        typeGUN = value;
    }

    private Object value = null;

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
