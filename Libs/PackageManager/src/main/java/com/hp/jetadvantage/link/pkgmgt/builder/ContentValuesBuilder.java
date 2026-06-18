package com.hp.jetadvantage.link.pkgmgt.builder;

import android.content.ContentValues;

import com.hp.jetadvantage.link.pkgmgt.PackageContract;

public class ContentValuesBuilder {
    private final ContentValues contentValues;

    public ContentValuesBuilder() {
        contentValues = new ContentValues();
    }

    public ContentValuesBuilder setSolutionId(String solutionId) {
        contentValues.put(PackageContract.PackageProviderEntry.SOLUTION_ID, solutionId);
        return this;
    }

    public ContentValuesBuilder setAgentID(String agentId) {
        contentValues.put(PackageContract.PackageProviderEntry.AGENT_ID, agentId);
        return this;
    }

    public ContentValuesBuilder setPackageName(String packageName) {
        contentValues.put(PackageContract.PackageProviderEntry.PACKAGE_NAME, packageName);
        return this;
    }

    public ContentValuesBuilder setFunctionType(String functionType) {
        contentValues.put(PackageContract.PackageProviderEntry.FUNCTION_TYPE, functionType);
        return this;
    }

    public ContentValuesBuilder setAgentName(String name) {
        contentValues.put(PackageContract.PackageProviderEntry.AGENT_NAME, name);
        return this;
    }

    public ContentValuesBuilder setMetadata(String metadata) {
        contentValues.put(PackageContract.PackageProviderEntry.METADATA, metadata);
        return this;
    }

    public ContentValuesBuilder setAgentDescription(String description) {
        contentValues.put(PackageContract.PackageProviderEntry.AGENT_DESCRIPTION, description);
        return this;
    }

    public ContentValuesBuilder setLaunchIntent(String launchIntent) {
        contentValues.put(PackageContract.PackageProviderEntry.LAUNCH_INTENT, launchIntent);
        return this;
    }

    public ContentValuesBuilder setExtData1(String param) {
        contentValues.put(PackageContract.PackageProviderEntry.EXT_DATA1, param);
        return this;
    }

    public ContentValuesBuilder setExtData2(String param) {
        contentValues.put(PackageContract.PackageProviderEntry.EXT_DATA2, param);
        return this;
    }

    public ContentValuesBuilder setExtData3(String param) {
        contentValues.put(PackageContract.PackageProviderEntry.EXT_DATA3, param);
        return this;
    }

    public ContentValues build() {
        return contentValues;
    }
}

