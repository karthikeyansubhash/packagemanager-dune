// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
package com.hp.jetadvantage.link.pkgmgt;

/**
 * PackageManager
 */
public class PackageManager {
    /** Package name of the service app */
    public static final String SERVICES_PACKAGE = "com.hp.jetadvantage.link.packagemanager";

    public static class VERSION {

        // Similar to android:versionCode, monotonically increasing
        public static final int LEVEL = VERSION_LEVEL.TWO;

        public static final String NO_VERSION = "0.0.0";

        /**
         * The version as a string
         */
        public static final String VERSION_NAME = "1.2.6";
    }

    public static class VERSION_LEVEL {
        public static final int UNDEFINED = -1;
        public static final int ONE = 1;   //PackageManager 1.0 (24.7.3)
        public static final int TWO = 2;   //PackageManager 1.1 (24.8)
    }
}
