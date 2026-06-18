/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import com.fasterxml.jackson.annotation.JsonValue;

public class E2Enumeration extends E2Type {

    protected String value;
    protected String jsonLiteral;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getJsonLiteral() {
        return jsonLiteral;
    }

    protected E2Enumeration() {
        this(null,null);
    }

    protected E2Enumeration(String value) {
        this(value, value);
    }

    protected E2Enumeration(String value, String jsonLiteral) {
        super();
        this.value = value;
        this.jsonLiteral = jsonLiteral;
    }
}
