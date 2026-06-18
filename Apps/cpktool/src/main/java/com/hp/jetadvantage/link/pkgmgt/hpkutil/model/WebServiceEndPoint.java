package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import com.google.gson.JsonObject;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.*;

public class WebServiceEndPoint {
    MethodType methodType;
    String category;
    String absolutePath;
    AuthType authType;

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty(PROPERTY_WEBSERVICE_METHOD, methodType.name().toLowerCase());
        object.addProperty(PROPERTY_WEBSERVICE_CATEGORY, category);
        object.addProperty(PROPERTY_WEBSERVICE_ABSOLUTEPATH, absolutePath);
        object.addProperty(PROPERTY_WEBSERVICE_AUTHTYPE, authType.getName().toLowerCase());
        return object;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(MethodType methodType) {
        this.methodType = methodType;
    }

    public void setMethodType(String methodType) throws Exception {
        if(methodType != null) {
            if (MethodType.GET.name().equalsIgnoreCase(methodType)) {
                this.methodType = MethodType.GET;
            } else if (MethodType.PUT.name().equalsIgnoreCase(methodType)) {
                this.methodType = MethodType.PUT;
            } else if (MethodType.POST.name().equalsIgnoreCase(methodType)) {
                this.methodType = MethodType.POST;
            } else if (MethodType.DELETE.name().equalsIgnoreCase(methodType)) {
                this.methodType = MethodType.DELETE;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public void setAuthType(String authType) throws IllegalArgumentException{
        if (AuthType.NONE.getName().equalsIgnoreCase(authType)) {
            this.authType = AuthType.NONE;
        } else if (AuthType.XAUTH.getName().equalsIgnoreCase(authType)) {
            this.authType = AuthType.XAUTH;
        } else if (AuthType.ADMIN.getName().equalsIgnoreCase(authType)) {
            this.authType = AuthType.ADMIN;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public enum MethodType{
        GET,
        PUT,
        POST,
        DELETE
    }

    public enum AuthType {
        NONE("none"),
        XAUTH("x-auth"),
        ADMIN("admin");

        AuthType(final String name) {
            this.name = name;
        }

        private final String name;

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
