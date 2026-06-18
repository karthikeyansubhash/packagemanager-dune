package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppInfo {
    private String icon;
    private String name;
    private String uuid;
    private String vendorName;
    private String installDate;
    private String version;
    private String packageName;
    private String platformType;
    private String description;

    public final String getIcon() {
        return icon;
    }

    public final void setIcon(final String icon) { this.icon = icon; }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final String getUuid() {
        return uuid;
    }

    public final void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public final String getVendorName() {
        return vendorName;
    }

    public final void setVendorName(final String vendorName) {
        this.vendorName = vendorName;
    }

    public final String getInstallDate() { return installDate; }

    public final void setInstallDate(final String installDate) { this.installDate = installDate; }

    public final String getVersion() { return version; }

    public final void setVersion(final String version) { this.version = version; }

    public final String getPackageName() {
        return packageName;
    }

    public final void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public final String getPlatformType() { return platformType; }

    public final void setPlatformType(final String platformType) { this.platformType = platformType; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

   public String toString() {
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
       String date = simpleDateFormat.format(new Date(Long.parseLong(installDate)));

       String des = null;
       try {
           Gson gson = new GsonBuilder().setPrettyPrinting().create();
           JsonElement jsonElement = gson.fromJson(description, JsonElement.class);
           des = gson.toJson(jsonElement);
       } catch (Exception e) {
           e.printStackTrace();
       }

       return "Detail{" +
               "name='" + name + '\'' +
               ", uuid='" + uuid + '\'' +
               ", vendorName='" + vendorName + '\'' +
               ", date='" + date + '\'' +
               ", version='" + version + '\'' +
               ", packageName=" + packageName +
               ", description=" + des +
               '}';
    }
}