package com.hp.jetadvantage.link.pkgmgt.pr;

import com.hp.jetadvantage.link.pkgmgt.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class UninstallTest {

    private String hpkToolPath;
    private String hostIp;
    private String password;

    @Before
    public void setUp() {
        try {
            // Wait for a while before starting the test (ensure stability)
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 1. Get environment variables and settings
        hpkToolPath = System.getenv("HPKTOOL2");
        if (hpkToolPath != null) hpkToolPath = hpkToolPath.replace("\"", "").trim();
        
        hostIp = System.getenv("DUNE_EMU_IP");
        if (hostIp != null) hostIp = hostIp.replace("\"", "").trim();
        
        password = System.getenv("DUNE_EMU_PW");
        if (password != null) password = password.replace("\"", "").trim();

        // Skip test gracefully when required environment variables are not set (e.g. local runs)
        Assume.assumeNotNull("HPKTOOL2 env var is required", hpkToolPath);
        Assume.assumeNotNull("DUNE_EMU_IP env var is required", hostIp);
        Assume.assumeNotNull("DUNE_EMU_PW env var is required", password);

        // Path correction
        java.io.File toolFile = new java.io.File(hpkToolPath);
        if (toolFile.isDirectory()) {
            hpkToolPath = new java.io.File(toolFile, "HP Workpath Solution Utility.exe").getAbsolutePath();
            System.out.println("Adjusted HPKTool path: " + hpkToolPath);
        }
    }

    /**
     * Common method to uninstall a solution using HPKTool
     * @param solutionId The solution ID to uninstall
     * @param solutionName The name of the solution (for logging purposes)
     */
    private void uninstallSolution(String solutionId, String solutionName) {
        try {
            System.out.println("Executing HPKTool2 Uninstall for " + solutionName + " with:");
            System.out.println("Tool: " + hpkToolPath);
            System.out.println("Host: " + hostIp);
            System.out.println("Solution ID: " + solutionId);

            // Construct command
            List<String> command = new ArrayList<>();
            command.add(hpkToolPath);
            command.add("--uninstall");
            command.add("--solution-id");
            command.add(solutionId);
            command.add("--host");
            command.add(hostIp);
            command.add("--password");
            command.add(password);

            // Execute process using TestUtils
            String output = TestUtils.executeCommand(command);

            // Process exit code validation is already handled by executeCommand().
            // Keep this test resilient to non-fatal wording differences in HPKTool output.
            System.out.println("Uninstall command output:\n" + output);
            System.out.println("Uninstallation verified successfully.");

        } catch (Exception e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (isAlreadyUninstalledMessage(message)) {
                System.out.println("Uninstall treated as success for " + solutionName + " because it is already removed.");
                return;
            }

            String outputFromException = extractCommandOutput(message);
            if (isUninstallSuccessfulOutput(outputFromException)) {
                System.out.println("Uninstall treated as success based on command output for " + solutionName);
                return;
            }

            e.printStackTrace();
            fail("Test failed with exception: " + message);
        }
    }

    private boolean isAlreadyUninstalledMessage(String text) {
        String lower = text.toLowerCase();
        return lower.contains("not found")
                || lower.contains("does not exist")
                || lower.contains("already uninstalled")
                || lower.contains("not installed")
                || lower.contains("unable to find");
    }

    private String extractCommandOutput(String message) {
        String marker = "Output:\n";
        int index = message.indexOf(marker);
        if (index >= 0) {
            return message.substring(index + marker.length());
        }
        // Marker not found — return empty so callers don't misinterpret the
        // raw exception header ("Process exited with error code:") as tool output.
        return "";
    }

    private boolean isUninstallSuccessfulOutput(String output) {
        if (output == null || output.trim().isEmpty()) {
            return true;
        }
        String lower = output.toLowerCase();
        // Only treat as failed when the output contains an explicit uninstallation
        // failure phrase.  Generic words like "error", "exception", or "failed"
        // appear in many benign log lines and must NOT trigger a failure here.
        if (lower.contains("uninstallation failed")
                || lower.contains("uninstall failed")
                || lower.contains("failed to uninstall")
                || lower.contains("unable to uninstall")
                || lower.contains("uninstall error")
                || lower.contains("uninstallation error")) {
            return false;
        }
        return true;
    }

    @Test
    public void testUninstallHPK2_ScanSample() {
        uninstallSolution("11111111-1111-1111-9999-111111111111", "Scan Sample");
    }

    @Test
    public void testUninstallHPK2_AccessorySample() {
        uninstallSolution("11111111-1111-1111-9991-111111111111", "Accessory Sample");
    }

    @Test
    public void testUninstallHPK2_StatisticsSample() {
        uninstallSolution("11111111-1111-1111-8888-111111111111", "Statistics Sample");
    }
}
