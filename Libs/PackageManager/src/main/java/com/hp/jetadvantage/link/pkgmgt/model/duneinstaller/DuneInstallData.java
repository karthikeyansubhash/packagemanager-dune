package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.hp.jetadvantage.link.pkgmgt.lib.SubApp;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static android.content.Intent.URI_INTENT_SCHEME;

public class DuneInstallData implements Parcelable {
    private String solutionUuid;
    private String applicationUuid;
    private String appName;
    private String packageName;
    private String versionName;
    private String launchIntent;
    private String installFilePath;
    private String metaData;
    private String vendorName;
    private String configFilePath;
    private String apkFilePath;
    private String scActivityName;
    private String scParentUuid;

    public DuneInstallData() {
    }

    public String getSolutionUuid() {
        return solutionUuid;
    }

    public void setSolutionUuid(String solutionUuid) {
        this.solutionUuid = solutionUuid;
    }

    public String getApplicationUuid() {
        return applicationUuid;
    }

    public void setApplicationUuid(String applicationUuid) {
        this.applicationUuid = applicationUuid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLaunchIntent() {
        return launchIntent;
    }

    public void setLaunchIntent(String launchIntent) {
        this.launchIntent = launchIntent;
    }

    public String getInstallFilePath() {
        return installFilePath;
    }

    public void setInstallFilePath(String installFilePath) {
        this.installFilePath = installFilePath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getApkFilePath() {
        return apkFilePath;
    }

    public void setApkFilePath(String apkFilePath) {
        this.apkFilePath = apkFilePath;
    }

    public String getScActivityName() {
        return scActivityName;
    }

    public void setScActivityName(String scActivityName) {
        this.scActivityName = scActivityName;
    }

    public String getScParentUuid() {
        return scParentUuid;
    }

    public void setScParentUuid(String scParentUuid) {
        this.scParentUuid = scParentUuid;
    }

    public static DuneInstallData setSubAppInfo(DuneInstallData DuneInstallData, SubApp subApp) {
        DuneInstallData newDuneInstallData = new DuneInstallData();
        newDuneInstallData.setSolutionUuid(DuneInstallData.getSolutionUuid());
        newDuneInstallData.setApplicationUuid(subApp.getUuid().toString());
        newDuneInstallData.setAppName(DuneInstallData.getAppName());
        newDuneInstallData.setPackageName(DuneInstallData.getPackageName());
        newDuneInstallData.setVersionName(DuneInstallData.getVersionName());
        newDuneInstallData.setMetaData(DuneInstallData.getMetaData());
        newDuneInstallData.setVendorName(DuneInstallData.getVendorName());
        newDuneInstallData.setScActivityName(subApp.getPlatformId());

        String activityName = subApp.getPlatformId();
        if (subApp.getPlatformId().contains("/")) {
            // remove package part before /
            String[] parts = subApp.getPlatformId().split("/");
            if (parts.length > 1) {
                activityName = parts[1];
            }
        }

        Intent sample = new Intent();
        sample.addCategory(Intent.CATEGORY_LAUNCHER);
        sample.setAction(Intent.ACTION_MAIN);
        sample.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | FLAG_ACTIVITY_CLEAR_TASK); //TODO : NEW_TASK
        sample.setPackage(DuneInstallData.getPackageName());
        sample.setClassName(DuneInstallData.getPackageName(), activityName);

        newDuneInstallData.setLaunchIntent(sample.toUri(URI_INTENT_SCHEME));
        newDuneInstallData.setInstallFilePath(DuneInstallData.getInstallFilePath());
        return newDuneInstallData;
    }

    @Override
    public String toString() {
        return "InstallInfo{" +
                "solutionUuid='" + solutionUuid + '\'' +
                ", applicationUuid=" + applicationUuid +
                ", packageName='" + packageName + '\'' +
                ", launchIntent='" + launchIntent + '\'' +
                ", installFilePath='" + installFilePath + '\'' +
                '}';
    }

    /**
     * @hide The client should not need to know about the parcelable methods
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @hide The client should not need to know about the parcelable methods
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(solutionUuid);
        dest.writeString(applicationUuid);
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeString(versionName);
        dest.writeString(launchIntent);
        dest.writeString(installFilePath);
        dest.writeString(metaData);
        dest.writeString(vendorName);
        dest.writeString(configFilePath);
        dest.writeString(apkFilePath);
        dest.writeString(scActivityName);
        dest.writeString(scParentUuid);
    }


    public DuneInstallData(Parcel in) {
        solutionUuid = in.readString();
        applicationUuid = in.readString();
        appName = in.readString();
        packageName = in.readString();
        versionName = in.readString();
        launchIntent = in.readString();
        installFilePath = in.readString();
        metaData = in.readString();
        vendorName = in.readString();
        configFilePath = in.readString();
        apkFilePath = in.readString();
        scActivityName = in.readString();
        scParentUuid = in.readString();
    }

    /**
     * @hide The client should not need to know about the parcelable methods
     */
    public static final Creator<DuneInstallData> CREATOR = new Creator<DuneInstallData>() {
        @Override
        public DuneInstallData createFromParcel(final Parcel source) {
            return new DuneInstallData(source);
        }

        @Override
        public DuneInstallData[] newArray(final int size) {
            return new DuneInstallData[size];
        }
    };
}

