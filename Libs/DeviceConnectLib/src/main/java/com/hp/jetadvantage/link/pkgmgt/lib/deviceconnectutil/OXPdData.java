package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

import java.util.HashMap;
import java.util.Map;

public class OXPdData {
    public String uuid;
    public Map<String, String> title = new HashMap<String, String>();
    public Map<String, String> description = new HashMap<String, String>();
    public String intentUri;
    public String icon;
    public Boolean setAsHome;

    @Override
    public String toString() {
        return "OXPdData{" +
                "uuid='" + uuid + '\'' +
                ", title=" + title +
                ", setAsHome=" + setAsHome +
                ", description=" + description +
                ", intentUri='" + intentUri + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }

    public String toLogString() {
        return "Log OXPdData{" +
                "uuid='" + uuid + '\'' +
                ", title(en-US)='" + title.get("en-US") + '\'' +
                ", setAsHome=" + setAsHome +
                ", description(en-US)='" + description.get("en-US") + '\'' +
                ", intentUri='" + intentUri + '\'' +
                '}';
    }
}
