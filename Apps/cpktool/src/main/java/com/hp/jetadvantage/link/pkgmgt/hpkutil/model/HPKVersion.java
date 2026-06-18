package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public enum HPKVersion {

    HPK_1_0("v2.1", 1, "/hpk.xsd"),
    HPK_1_1("v2.2", 2, "/hpk_2.2.xsd"),
    HPK_1_2("v2.3", 3, "/hpk_2.3.xsd"),
    HPK_1_3("v2.4", 4, "/hpk_2.4.xsd"),
    HPK_1_4("v2.5", 5, "/hpk_2.5.xsd");

    private final String hpkVersion;
    private final int level;
    private final String xsdPath;

    HPKVersion(String hpkVersion, int level, String xsdPath) {
        this.hpkVersion = hpkVersion;
        this.level = level;
        this.xsdPath = xsdPath;
    }

    @Override
    public String toString() {
        return hpkVersion;
    }

    public int getLevel() {
        return level;
    }

    public String getXsdPath() {
        return xsdPath;
    }

    public static HPKVersion getHPKVersion(String versionStr) {
        if(versionStr != null) {
            for(HPKVersion version: HPKVersion.values()) {
                if(versionStr.toLowerCase().equalsIgnoreCase(version.hpkVersion.toLowerCase())) {
                    return version;
                }
            }
        }
        return null;
    }
}
