package com.hp.jetadvantage.link.packagemanager.hpkutil.test.value;

import java.util.UUID;

public class ConfigSampleTest {
    public static final UUID APP_UUID = UUID.fromString("11111111-1111-1111-9995-111111111111");
    public static final UUID WRONG_APP_UUID = UUID.fromString("11111111-1111-1111-9995-11111111111123");
    public static final String APP_NAME = "Config Sample";
    public static final String VENDOR_NAME = "HP";
    public static final String APP_DATE = "20200518";
    public static final String INSTALL_FILE = "ConfigSample.apk";
    public static final String SCHEMA_LOCATION = "http://www.hp.com/schemas/jetadvantage/link/hpk/v2.4 hpk.xsd";
}
