package com.hp.jetadvantage.link.pkgmgt;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Presetting {

    // This solution must never be uninstalled, even during full cleanup.
    private static final String PROTECTED_SOLUTION_ID = "b081727d-bb2e-46f6-94c0-5f17c4587b16";

    public static void uninstallAllSolutions(String hpkToolPath, String hostIp, String password) {
        try {
            System.out.println("Checking all installed solutions...");

            List<String> listCommand = new ArrayList<>();
            listCommand.add(hpkToolPath);
            listCommand.add("--solution-list");
            listCommand.add("--host");
            listCommand.add(hostIp);
            listCommand.add("--password");
            listCommand.add(password);

            String output = TestUtils.executeCommand(listCommand);
            
            // Parse Output
            String jsonString = extractJsonArray(output);
            if (jsonString == null) {
                System.out.println("Failed to parse solution list output or no solutions found.");
                return;
            }

            JSONArray solutions = new JSONArray(jsonString);
            if (solutions.length() == 0) {
                System.out.println("No solutions installed.");
                return;
            }

            System.out.println("Found " + solutions.length() + " solution(s) installed.");

            for (int i = 0; i < solutions.length(); i++) {
                JSONObject solution = solutions.getJSONObject(i);
                String solutionId = solution.optString("solutionId");
                String solutionName = solution.optString("name", "Unknown");

                if (!solutionId.isEmpty()) {
                    if (PROTECTED_SOLUTION_ID.equalsIgnoreCase(solutionId)) {
                        System.out.println("Skipping protected solution: " + solutionName + " (" + solutionId + ")");
                        continue;
                    }
                    System.out.println("Uninstalling solution: " + solutionName + " (" + solutionId + ")");
                    uninstallSolution(hpkToolPath, hostIp, password, solutionId);
                }
            }

            System.out.println("All solutions have been uninstalled.");

        } catch (Exception e) {
            System.out.println("Error during uninstalling all solutions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void uninstallSolution(String hpkToolPath, String hostIp, String password, String solutionId) {
        try {
            List<String> uninstallCommand = new ArrayList<>();
            uninstallCommand.add(hpkToolPath);
            uninstallCommand.add("--uninstall");
            uninstallCommand.add("--solution-id");
            uninstallCommand.add(solutionId);
            uninstallCommand.add("--host");
            uninstallCommand.add(hostIp);
            uninstallCommand.add("--password");
            uninstallCommand.add(password);

            TestUtils.executeCommand(uninstallCommand);
            System.out.println("Uninstallation of " + solutionId + " completed.");
        } catch (Exception e) {
            System.out.println("Error during uninstalling solution " + solutionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String extractJsonArray(String output) {
        int startIndex = output.indexOf("[");
        int endIndex = output.lastIndexOf("]");
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return output.substring(startIndex, endIndex + 1);
        }
        return null;
    }
}
