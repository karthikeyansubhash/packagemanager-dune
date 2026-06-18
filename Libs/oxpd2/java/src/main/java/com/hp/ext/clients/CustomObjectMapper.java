/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.clients;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.databind.DeserializationFeature;

public class CustomObjectMapper<T> extends com.fasterxml.jackson.databind.ObjectMapper {

    private Class<T> tClass;

    public CustomObjectMapper(Class<T> tClass) {
        this.tClass = tClass;
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Serializes the given JSON string into the object.
     */
    public T readValue(String content) {
        try {
            if (content != null && !content.isEmpty()) {
                T value = this.readValue(content, tClass);
                return value;
            }
            return null;
        } catch (IOException ioe) {
            throw new CompletionException(ioe);
        }
    }
}
