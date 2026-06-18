package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

public class OSValidator {

    private static String OPERATING_SYSTEM = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OPERATING_SYSTEM.contains("win");
    }

    public static boolean isUnix() {
        return (OPERATING_SYSTEM.contains("nix") || OPERATING_SYSTEM.contains("nux") || OPERATING_SYSTEM.contains("aix"));
    }

    public static OS getOS(){
        if (isWindows()) {
            return OS.WINDOWS;
        } else if (isUnix()) {
            return OS.UNIX;
        } else {
            return OS.UNKNOWN;
        }
    }

    public enum OS {
        WINDOWS, UNIX, UNKNOWN
    }
}
