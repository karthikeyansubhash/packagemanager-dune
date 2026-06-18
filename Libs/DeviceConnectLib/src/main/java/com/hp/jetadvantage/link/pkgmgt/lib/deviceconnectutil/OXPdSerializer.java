package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class OXPdSerializer implements JsonSerializer<OXPdData> {

    @Override
    public JsonElement serialize(OXPdData data, Type type, JsonSerializationContext jsc) {
        Gson gson = new Gson();
        JsonObject jObj = (JsonObject)gson.toJsonTree(data);
        if(data.setAsHome == null || data.setAsHome == false){
            jObj.remove("setAsHome");
        }
        return jObj;
    }
}
