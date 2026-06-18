package com.hp.jetadvantage.link.pkgmgt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtils {

    /**
     * Helper method to execute ADB commands.
     * Usage: runAdbCommand(hostIp, "shell", "content", "query", "--uri", "content://...")
     * 
     * @param hostIp The IP address of the target device (e.g., "192.168.1.10"). 
     *               If null or empty, the "-s" flag will be omitted (adb will target the only connected device).
     * @param args   The arguments for the adb command (e.g., "shell", "ls", "-l").
     * @return The standard output (and error output) of the command.
     */
    public static String runAdbCommand(String hostIp, String... args) {
        try {
            List<String> command = new ArrayList<>();
            command.add("adb");
            // Target specific device using the host IP
            if (hostIp != null && !hostIp.isEmpty()) {
                command.add("-s");
                command.add(hostIp + ":5555");
            }
            command.addAll(Arrays.asList(args));

            return executeCommand(command);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing ADB command: " + e.getMessage();
        }
    }

    /**
     * Helper method to execute generic system commands.
     * 
     * @param command The list of command arguments.
     * @return The standard output (and error output) of the command.
     * @throws Exception If the command fails to execute or returns a non-zero exit code.
     */
    public static String executeCommand(List<String> command) throws Exception {
        System.out.println("Executing Command: " + String.join(" ", command));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // Print log to console for real-time feedback
            output.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Process exited with error code: " + exitCode + "\nOutput:\n" + output.toString());
        }
        
        return output.toString();
    }

    /**
     * Helper method to query a Content Provider.
     * 
     * @param hostIp The IP of the device.
     * @param uri The content URI (e.g., "content://settings/system").
     * @param selection The SQL selection clause (e.g., "package_name='com.hp.test'"). Can be null.
     * @return The query result.
     */
    public static String queryContentProvider(String hostIp, String uri, String selection) {
        List<String> args = new ArrayList<>();
        args.add("shell");
        args.add("content");
        args.add("query");
        args.add("--uri");
        args.add(uri);
        
        if (selection != null && !selection.isEmpty()) {
            args.add("--where");
            args.add(selection);
        }
        
        return runAdbCommand(hostIp, args.toArray(new String[0]));
    }

    /**
     * Helper method to run an Android Instrumented Test on the device.
     * 
     * @param hostIp The IP of the device.
     * @param testClass The full class name of the test (e.g., "com.example.MyTest").
     * @param testMethod The method name to run (e.g., "testMyFeature").
     * @param extraArgs Key-value pairs for test arguments (e.g., "targetPackage", "com.example.app").
     * @return The output of the instrumentation command.
     */
    public static String runDeviceTest(String hostIp, String testClass, String testMethod, String... extraArgs) {
        List<String> args = new ArrayList<>();
        args.add("shell");
        args.add("am");
        args.add("instrument");
        args.add("-w");
        args.add("-r");
        
        // Add test class and method
        args.add("-e");
        args.add("class");
        args.add(testClass + "#" + testMethod);
        
        // Add extra arguments
        for (int i = 0; i < extraArgs.length; i += 2) {
            if (i + 1 < extraArgs.length) {
                args.add("-e");
                args.add(extraArgs[i]);
                args.add(extraArgs[i+1]);
            }
        }
        
        // Add runner
        args.add("com.hp.jetadvantage.link.pkgmgt.test/androidx.test.runner.AndroidJUnitRunner");
        
        return runAdbCommand(hostIp, args.toArray(new String[0]));
    }
}
