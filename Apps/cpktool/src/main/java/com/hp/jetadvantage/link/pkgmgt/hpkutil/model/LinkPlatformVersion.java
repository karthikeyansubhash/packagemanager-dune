package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public enum LinkPlatformVersion {

    LINK_PLATFORM_19_3("19.3", HPKVersion.HPK_1_3),
    LINK_PLATFORM_19_4("19.4", HPKVersion.HPK_1_3),
    LINK_PLATFORM_26_4("26.4", HPKVersion.HPK_1_3),
    LINK_PLATFORM_29_4("29.4", HPKVersion.HPK_1_3),
    LINK_PLATFORM_29_5("29.5", HPKVersion.HPK_1_4),
    LINK_PLATFORM_29_6("29.6", HPKVersion.HPK_1_4),
    LINK_PLATFORM_29_7("29.7", HPKVersion.HPK_1_4),
    LINK_PLATFORM_31_7("31.7", HPKVersion.HPK_1_4),
    LINK_PLATFORM_31_8("31.8", HPKVersion.HPK_1_4);

    private final String platformVersion;
    private final HPKVersion hpkVersion;

    LinkPlatformVersion(String platformVersion, HPKVersion hpkVersion) {
        this.platformVersion = platformVersion;
        this.hpkVersion = hpkVersion;
    }

    /**
     * <p>getEnumByValue</p>
     * @param value platformVersion String
     * @return LinkPlatformVersion that matches with input string
     */
    public static LinkPlatformVersion getEnumByValue(String value) {
        for (LinkPlatformVersion linkPlatformVersion : LinkPlatformVersion.values()) {
            if (linkPlatformVersion.toString().equalsIgnoreCase(value))
                return linkPlatformVersion;
        }
        return null;
    }

    /**
     * <p>getEnumByHPKVersion</p>
     * @param hpkVersion
     * @return Find latest LinkPlatformVersion using input hpkVersion
     */
    public static LinkPlatformVersion getEnumByHPKVersion(HPKVersion hpkVersion) {
        if (hpkVersion.getLevel() < HPKVersion.HPK_1_3.getLevel()) {
            return null;
        }
        LinkPlatformVersion ret = LinkPlatformVersion.LINK_PLATFORM_19_3;
        for (LinkPlatformVersion linkPlatformVersion : LinkPlatformVersion.values()) {
            if(linkPlatformVersion.hpkVersion.getLevel() == hpkVersion.getLevel()) {
                ret = linkPlatformVersion;
            }
        }
        return ret;
    }

    /**
     * <p>checkPlatformVersion</p>
     * @param hpkVersion
     * @return true if input hpkVersion is less than HPK 1.3 or LinkPlatformVersion and HpkVersion are matched.
     */
    public boolean checkPlatformVersion(HPKVersion hpkVersion) {
        if (hpkVersion.getLevel() < HPKVersion.HPK_1_3.getLevel()) {
            return true;
        }
        return this.hpkVersion == hpkVersion;
    }

    public HPKVersion getHpkVersion() {
        return hpkVersion;
    }

    @Override
    public String toString() {
        return platformVersion;
    }
}
