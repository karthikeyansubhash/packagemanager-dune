package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import com.google.gson.JsonObject;

public class Configuration {
    private JsonObject data;
    private String schema;
    private String uuid;

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
