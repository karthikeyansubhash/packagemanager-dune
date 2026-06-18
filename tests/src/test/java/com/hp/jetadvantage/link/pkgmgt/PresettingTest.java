package com.hp.jetadvantage.link.pkgmgt;

import org.junit.Before;
import org.junit.Test;

public class PresettingTest {

    private String hpkToolPath;
    private String hostIp;
    private String password;

    @Before
    public void setUp() {
        // 1. Get environment variables and settings
        hpkToolPath = System.getenv("HPKTOOL2");
        hostIp = System.getenv("DUNE_EMU_IP");
        password = System.getenv("DUNE_EMU_PW");
        
        // Default values for local testing
        if (hpkToolPath == null) hpkToolPath = "C:\\HPKTool2\\latest\\HP Workpath Solution Utility.exe";
        if (hostIp == null) hostIp = "15.38.181.219";
        if (password == null) password = "workpath@123";

        // Path correction
        java.io.File toolFile = new java.io.File(hpkToolPath);
        if (toolFile.isDirectory()) {
            hpkToolPath = new java.io.File(toolFile, "HP Workpath Solution Utility.exe").getAbsolutePath();
        }
    }

    @Test
    public void ensureAppsUninstalled() {
        System.out.println("Starting Presetting: Ensuring all apps are uninstalled...");

        Presetting.uninstallAllSolutions(hpkToolPath, hostIp, password);

        System.out.println("Presetting completed.");
    }
}
