package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JsonHelper {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            })
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(date.getTime());
                }
            })
            .create();

    public static String toJson(final Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(final String json, final Type typeOff) {
        return GSON.fromJson(json, typeOff);
    }

    public static <T> T fromJson(final String json, final Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJson(final InputStream stream, final Class<T> clazz) {
        return GSON.fromJson(new InputStreamReader(stream), clazz);
    }

    public static <T> List<T> fromJsonToList(final InputStream stream, Class<T[]> clazz) {
        T[] arr = GSON.fromJson(new InputStreamReader(stream), clazz);
        return Arrays.asList(arr);
    }

    public static boolean isValid(final String json) {
        try {
            GSON.fromJson(json, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}
