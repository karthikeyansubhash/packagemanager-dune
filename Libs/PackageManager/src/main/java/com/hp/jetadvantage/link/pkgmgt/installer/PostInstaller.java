package com.hp.jetadvantage.link.pkgmgt.installer;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static android.content.Intent.URI_INTENT_SCHEME;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.ext.clients.CustomObjectMapper;
import com.hp.ext.service.application.ApplicationAgentRegistrationRecord;
import com.hp.ext.service.application.MessageCenterAgentRegistrationRecord;
import com.hp.ext.service.authentication.AuthenticationAgentRegistrationRecord;
import com.hp.ext.service.copy.CopyAgentRegistrationRecord;
import com.hp.ext.service.deviceUsage.DeviceUsageAgentRegistrationRecord;
import com.hp.ext.service.jobStatistics.JobStatisticsAgentRegistrationRecord;
import com.hp.ext.service.printJob.PrintJobAgentRegistrationRecord;
import com.hp.ext.service.scanJob.ScanJobAgentRegistrationRecord;
import com.hp.ext.service.security.SecurityAgentRegistrationRecord;
import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord;
import com.hp.ext.service.supplies.SuppliesAgentRegistrationRecord;
import com.hp.ext.service.usbAccessories.UsbAccessoriesAgentRegistrationRecord;
import com.hp.ext.service.usbAccessories.UsbRegistrationIdentification;
import com.hp.ext.types.application.ApplicationCategory;
import com.hp.ext.types.solutionManager.RegistrationRecord;
import com.hp.ext.types.solutionManager.SolutionContent;
import com.hp.ext.types.solutionManager.SolutionState;
import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;
import com.hp.jetadvantage.link.pkgmgt.builder.ContentValuesBuilder;
import com.hp.jetadvantage.link.pkgmgt.controller.MessageController;
import com.hp.jetadvantage.link.pkgmgt.helper.DatabaseHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.EventReceiverManager;
import com.hp.jetadvantage.link.pkgmgt.helper.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.PackageHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.SolutionManifestHelper;
import com.hp.jetadvantage.link.pkgmgt.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.ApkInfo;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.Accessory;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.AppManagement;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.Details;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.InstallStatus;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data.PlatformMessage;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions.Solutions;
import com.hp.jetadvantage.link.pkgmgt.uninstaller.PreUninstaller;
import com.hp.jetadvantage.link.pkgmgt.utils.CDM;
import com.hp.jetadvantage.link.pkgmgt.utils.SystemCall;
import com.hp.jetadvantage.link.pkgmgt.utils.Utils;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PostInstaller extends MessageController {

    private static final String TAG = Constants.TAG + "PostInstaller";

    private int statusCode;
    private Solutions solutions;
    private String packageName;
    private String message;
    private String filePath;

    public PostInstaller(int statusCode, Solutions solutions, String packageName, String message, String filePath) {
        this.statusCode = statusCode;
        this.solutions = solutions;
        this.packageName = packageName;
        this.message = message;
        this.filePath = filePath;
    }

    public PostInstaller(int statusCode, String error) {
        this.statusCode = statusCode;
        this.message = error;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onServiceConnected() {
        // onStart() -> onServiceConnected()
        onPackageInstalled();
    }

    public void sendInstallMessage(InstallStatus installStatus) {
        int responseCode = 0;
        PlatformMessage message = new PlatformMessage();
        AppManagement appManagement = new AppManagement();
        Details details = new Details();

        if (PackageInstallerState.isCompleted.name().equals(installStatus.getStatus())) {
            responseCode = HttpURLConnection.HTTP_OK;
        } else if (PackageInstallerState.isInProgress.name().equals(installStatus.getStatus())) {
            responseCode = HttpURLConnection.HTTP_ACCEPTED;
        } else if (PackageInstallerState.isFailed.name().equals(installStatus.getStatus())) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        }
        details.setInstallStatus(installStatus);
        appManagement.setDetails(details);
        message.setAppManagement(appManagement);
        sendMessage(responseCode, JsonHelper.toJson(message, false));
    }

    public void onPackageInstalled() {
        Log.i(TAG, "onPackageInstalled");
        if (statusCode == PackageInstaller.STATUS_SUCCESS ||
                statusCode == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            try {
                PackageManager pm = PackageManagerApplication.getAppContext().getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                int uid = pm.getApplicationInfo(packageName, 0).uid;
                UserHandle userHandle = UserHandle.getUserHandleForUid(uid);

                if (packageInfo.requestedPermissions != null) {
                    for (String permission : packageInfo.requestedPermissions) {
                        if (permission.contains("android.permission.")) {
                            try {
                                SystemCall.grantRuntimePermission(packageName, permission, userHandle);
                                Log.i(TAG, "EV/Permission is granted: " + packageName);
                            } catch (Exception ignored) {
                            }
                        } else {
                            Log.i(TAG, "EV/Permission is skipped: " + packageName);
                        }
                    }
                } else {
                    Log.i(TAG, "requestedPermissions is null");
                }
            } catch (PackageManager.NameNotFoundException nameError) {
                Log.e(TAG, "Package is not installed: " + packageName);
                Log.e(TAG, "nameError: " + nameError.getMessage());
                statusCode = PackageInstaller.STATUS_FAILURE;
                message = nameError.getMessage();
            } catch (Exception e) {
                Log.e(TAG, "Package error: " + packageName + ", " + e.getMessage());
                statusCode = PackageInstaller.STATUS_FAILURE;
                message = e.getMessage();
            }
        }

        try {
            if (statusCode == PackageInstaller.STATUS_SUCCESS) {
                Log.i(TAG, "Install complete message received: installer, result=" + statusCode);
                ApkInfo apkInfo = populateApkInfo(getContext(), filePath, packageName);
                sendInstallMessage(new InstallStatus(PackageInstallerState.isCompleted.name(), solutions.getSolutionId()));
                CompletableFuture.runAsync(() -> finishInstallation(solutions, apkInfo));
            } else {
                Log.e(TAG, "Install STATUS_FAILURE " + statusCode + ", message: " + message);
                ContentValues cv = DatabaseHelper.buildInstallerValues(solutions, packageName, PackageInstallerState.isFailed, message);
                DatabaseHelper.upsertInstaller(cv);
                sendInstallMessage(new InstallStatus(PackageInstallerState.isFailed.name(), solutions.getSolutionId(), message));
            }
        } catch (Exception e) {
            // ApplicationAgentRegistrationRecord parsing할 때 발생 가능한 exception
            sendInstallMessage(new InstallStatus(PackageInstallerState.isFailed.name(), solutions.getSolutionId(), e.getMessage()));
        } finally {
            try {
                Utils.clearCache(PackageManagerApplication.getAppContext());
                Log.i(TAG, "clearCache: done");
            } catch (Exception e) {
                Log.e(TAG, "clearCache: " + e.getMessage());
            }
        }
    }

    private ApkInfo populateApkInfo(Context context, String filePath, String packageName) throws PackageManager.NameNotFoundException {
        ApkInfo apkInfo = new ApkInfo();
        String hash = PackageHelper.getApplicationHash(filePath);
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        String versionName = packageInfo.versionName;
        apkInfo.setPackageName(packageName);
        apkInfo.setPath(filePath);
        apkInfo.setHash(hash);
        apkInfo.setVersionName(versionName);
        return apkInfo;
    }

    private InstallStatus finishInstallation(Solutions solutions, ApkInfo apkInfo) {
        String solutionId = solutions.getSolutionId();
        try {
            Context context = PackageManagerApplication.getAppContext();
            Pair<String, SolutionContent> pair = getSolutionsAsync(solutionId).get();
            String solutionJson = pair.first;
            SolutionContent solutionContent = pair.second;

            if (solutionContent == null) {
                throw new Exception("Failed to get solution content");
            }

            if (!solutionContent.getState().equals(SolutionState.SsInstalled)) {
                throw new Exception("install state is not installed.");
            }

            ApplicationAgentRegistrationRecord mainRecord = SolutionManifestHelper.getMainRecord(solutionContent.getRegistrations());
            String mainApplicationId = null;
            if (mainRecord != null) {
                mainApplicationId = mainRecord.getAgentId().toString();
            } else {
                List<AuthenticationAgentRegistrationRecord> authRecords = SolutionManifestHelper.getAuthenticationRecords(solutionContent.getRegistrations());
                if (!authRecords.isEmpty()) {
                    mainApplicationId = authRecords.get(0).getAgentId().toString();
                }
            }

            if (mainApplicationId == null) {
                throw new Exception("There is no main record");
            }

            Log.d(TAG, "solutionId: " + solutionId);
            String allowListType = (solutions.getAllowListType() != null) ? solutions.getAllowListType().name() : null;

            //1-1. DB Update (installer)
            updateInstallerDatabase(solutionContent, apkInfo.getPackageName());

            //1-2. DB Update (packages)
            boolean hasApplicationRecords = false;
            for (RegistrationRecord record : solutionContent.getRegistrations()) {
                String targetTypeGUN = new ApplicationAgentRegistrationRecord().getTypeGUN();
                Log.d(TAG, "getTypeGUN: " + record.getRecord().getTypeGUN());
                if (record.getRecord().getTypeGUN().contains(targetTypeGUN)) {
                    ApplicationAgentRegistrationRecord appRecord =
                            SolutionManifestHelper.getRegistrationRecordFromRecordValue(record.getRecord().getValue(), ApplicationAgentRegistrationRecord.class);
                    updatePackagesDatabase(solutionContent, appRecord, apkInfo, solutionJson, allowListType);
                    hasApplicationRecords = true;
                }
            }

            if (!hasApplicationRecords) {
                // If there is no ApplicationRecords, we just add basic appInfo in Packages table.
                updatePackagesDatabase(solutionContent, apkInfo, solutionJson, allowListType);
            }

            //2. update provider databases
            updateProviders(solutionContent, apkInfo.getPackageName(), solutionJson);

            //3. update attestation databases
            updateAttestation(apkInfo.getHash(), solutionId);

            //4. Register event receivers (DUNE-314377)
            EventReceiverManager eventReceiverManager = new EventReceiverManager(context);
            boolean registered = eventReceiverManager.registerEventReceivers(apkInfo.getPackageName());
            if (!registered) {
                Log.w(TAG, "Event receiver registration did not create rows for package=" + apkInfo.getPackageName());
            }

            //5. notify finish installation
            String action = PackageContract.Intent.ACTION_PACKAGE_INSTALLED;

            Bundle extras = new Bundle(1);
            extras.putString(PackageContract.Intent.EXTRA_APPLICATION_AGENT_ID, mainApplicationId);
            extras.putString(PackageContract.Intent.EXTRA_SOLUTION_ID, solutionId);
            extras.putString(PackageContract.Intent.EXTRA_CLIENT_ID, "ciJamc");
            extras.putString(PackageContract.Intent.EXTRA_INSTALL_SOURCE, "isStandardRepository");
            extras.putString(PackageContract.Intent.EXTRA_PACKAGE, packageName);

            Utils.sendPackageBroadcast(context, action, extras, PackageContract.Permission.PACKAGE_LIFECYCLE_EVENTS);

            Log.i(TAG, "InstallStatus: " + PackageInstallerState.isCompleted.name());
            return new InstallStatus(PackageInstallerState.isCompleted.name(), solutionContent.getSolutionId().toString());
        } catch (Exception e) {
            Log.e(TAG, "update provider databases failed: " + e.getMessage(), e);
            InstallStatus installStatus = new InstallStatus(PackageInstallerState.isFailed.name(), solutionId, "failed: " + e.getMessage());

            PreUninstaller preUninstaller = new PreUninstaller(solutionId);
            preUninstaller.uninstall(PackageManagerApplication.getAppContext());
            return installStatus;
        }
    }

    private CompletableFuture<Pair<String, SolutionContent>> getSolutionsAsync(String solutionId) {
        if (solutionId == null) {
            return CompletableFuture.completedFuture(new Pair<>(null, null));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                while (true) {
                    String response = CDM.getSolutions(solutionId).get();
                    CustomObjectMapper<SolutionContent> objectMapper = new CustomObjectMapper<>(SolutionContent.class);
                    SolutionContent solutionContent = objectMapper.readValue(response);
                    if (solutionContent != null && SolutionState.SsInstalling.equals(solutionContent.getState())) {
                        Log.d(TAG, "Solution state is installing, retrying in 1 second..." + solutionId);
                        Thread.sleep(1000);
                        continue;
                    }
                    return new Pair<>(response, solutionContent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching solutions", e);
                return new Pair<>(null, null);
            }
        });
    }

    private String getRawMetadata(String solutionJson, String typeGUN, String agentId) {
        if (solutionJson == null || typeGUN == null) {
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(solutionJson).getAsJsonObject();
            if (!root.has("registrationRecords")) return null;

            JsonArray records = root.getAsJsonArray("registrationRecords");
            for (JsonElement element : records) {
                JsonObject recordObj = element.getAsJsonObject();
                if (!recordObj.has("record")) continue;

                JsonObject record = recordObj.getAsJsonObject("record");
                if (!record.has("typeGUN") || !typeGUN.equals(record.get("typeGUN").getAsString())) {
                    continue;
                }

                if (!record.has("value")) continue;
                JsonObject value = record.getAsJsonObject("value");
                if (value.has("agentId") && agentId.equals(value.get("agentId").getAsString())) {
                    return value.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse raw metadata for " + typeGUN, e);
        }
        return null;
    }

    protected Context getContext() {
        return PackageManagerApplication.getAppContext();
    }

    /**
     * Update agent information in provider table
     *
     * @param
     * @throws Exception
     */
    protected void updateProviders(SolutionContent solutionContent, String packageName, String solutionJson) throws Exception {
        String solutionId = solutionContent.getSolutionId().toString();
        int deletedCount = uninstallProviders(solutionId);
        if (deletedCount > 0) {
            Log.i(TAG, "Package providers are deleted items " + deletedCount + ": " + solutionId);
        }

        Context context = getContext();
        PackageManager pm = context.getPackageManager();

        // Print
        PrintJobAgentRegistrationRecord printRecord =
                SolutionManifestHelper.getPrintJobAgentRegistrationRecord(solutionContent.getRegistrations());
        if (printRecord != null) {
            String agentUuid = (printRecord.getAgentId() != null) ? printRecord.getAgentId().toString() : null;
            String name = (printRecord.getName() != null) ? printRecord.getName().toString() : null;
            String description = (printRecord.getLocalizedDescription() != null && printRecord.getLocalizedDescription().getStringId() != null) ?
                    printRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, printRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(printRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(printRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(printRecord.getAgentId().toString(), pcv);
        }

        // Scan
        ScanJobAgentRegistrationRecord scanRecord =
                SolutionManifestHelper.getScanJobAgentRegistrationRecord(solutionContent.getRegistrations());
        if (scanRecord != null) {
            String agentUuid = (scanRecord.getAgentId() != null) ? scanRecord.getAgentId().toString() : null;
            String name = (scanRecord.getName() != null) ? scanRecord.getName().toString() : null;
            String description = (scanRecord.getLocalizedDescription() != null && scanRecord.getLocalizedDescription().getStringId() != null) ?
                    scanRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, scanRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(scanRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(scanRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(scanRecord.getAgentId().toString(), pcv);
        }

        // Copy
        CopyAgentRegistrationRecord copyRecord =
                SolutionManifestHelper.getCopyAgentRegistrationRecord(solutionContent.getRegistrations());
        if (copyRecord != null) {
            String agentUuid = (copyRecord.getAgentId() != null) ? copyRecord.getAgentId().toString() : null;
            String name = (copyRecord.getName() != null) ? copyRecord.getName().toString() : null;
            String description = (copyRecord.getLocalizedDescription() != null && copyRecord.getLocalizedDescription().getStringId() != null) ?
                    copyRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, copyRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(copyRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(copyRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(copyRecord.getAgentId().toString(), pcv);
        }

        // HomeScreen, Launcher
        ApplicationAgentRegistrationRecord appRecord = SolutionManifestHelper.getMainRecord(solutionContent.getRegistrations());
        Log.i(TAG, "Find provider for HOMESCREEN");
        if (appRecord != null && ApplicationCategory.AcKiosk.equals(appRecord.getCategory())) {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setAction(Intent.ACTION_MAIN);
            List<ResolveInfo> queryIntentActivities = pm.queryIntentActivities(intent, 0);
            boolean launcherFlag = false;
            for (ResolveInfo resolveInfo : queryIntentActivities) {
                if (packageName != null && packageName.equalsIgnoreCase(resolveInfo.activityInfo.packageName)) {
                    Log.i(TAG, "HOMESCREEN app should be Launcher. It's OK.");
                    launcherFlag = true;
                    break;
                }
            }
            if (!launcherFlag) {
                //If application is not a Launcher, installation must be failed with error.
                Log.e(TAG, "Failed to find Launcher in app");
            } else {
                String agentUuid = (appRecord.getAgentId() != null) ? appRecord.getAgentId().toString() : null;
                String name = (appRecord.getName() != null) ? appRecord.getName().toString() : null;
                String description = (appRecord.getLocalizedDescription() != null && appRecord.getLocalizedDescription().getStringId() != null) ?
                        appRecord.getLocalizedDescription().getStringId().getValue() : null;

                String rawMetadata = getRawMetadata(solutionJson, appRecord.getTypeGUN(), agentUuid);
                if (rawMetadata == null) rawMetadata = JsonHelper.toJson(appRecord);

                ContentValues pcv = new ContentValuesBuilder()
                        .setSolutionId(solutionId)
                        .setAgentID(agentUuid)
                        .setPackageName(packageName)
                        .setFunctionType(appRecord.getTypeGUN())
                        .setMetadata(rawMetadata)
                        .setAgentName(name)
                        .setAgentDescription(description)

                        .build();
                DatabaseHelper.upsertProvider(appRecord.getAgentId().toString(), pcv);

                // Update IS_HOME_SCREEN_APP in packages table
                ContentValues homeCv = new ContentValues();
                homeCv.put(PackageContract.PackageEntry.IS_HOME_SCREEN_APP, true);
                DatabaseHelper.updatePackage(homeCv,
                        PackageContract.PackageEntry.SOLUTION_ID + " = ?", new String[]{solutionId});
            }
        }

        // Authentication
        List<AuthenticationAgentRegistrationRecord> authRecords =
                SolutionManifestHelper.getAuthenticationRecords(solutionContent.getRegistrations());

        if (!authRecords.isEmpty()) {
            Log.i(TAG, "Find provider for authRecords");
            for (AuthenticationAgentRegistrationRecord authProvider : authRecords) {
                String primaryUUID = UUID.randomUUID().toString();
                String activityName = getActivityName(authProvider.getAuthenticationTarget().getWorkpath().getPackage().getValue());
                Intent launchIntent = createLaunchIntent(new ComponentName(packageName, activityName));

                String agentUuid = (authProvider.getAgentId() != null) ? authProvider.getAgentId().toString() : null;
                String name = (authProvider.getName() != null) ? authProvider.getName().toString() : null;
                String description = (authProvider.getLocalizedDescription() != null && authProvider.getLocalizedDescription().getStringId() != null) ?
                        authProvider.getLocalizedDescription().getStringId().getValue() : null;

                String rawMetadata = getRawMetadata(solutionJson, authProvider.getTypeGUN(), agentUuid);
                if (rawMetadata == null) rawMetadata = JsonHelper.toJson(authProvider);

                ContentValues pcv = new ContentValuesBuilder()
                        .setSolutionId(solutionId)
                        .setAgentID(agentUuid)
                        .setPackageName(packageName)
                        .setFunctionType(authProvider.getTypeGUN())
                        .setMetadata(rawMetadata)
                        .setAgentName(name)
                        .setAgentDescription(description)
                        .setLaunchIntent(launchIntent.toUri(URI_INTENT_SCHEME))

                        .build();
                DatabaseHelper.upsertProvider(primaryUUID, pcv);
            }
        }

        // accessories
        UsbAccessoriesAgentRegistrationRecord accessoryRecord =
                SolutionManifestHelper.getUsbAccessoriesRecord(solutionContent.getRegistrations());
        if (accessoryRecord != null) {
            Log.i(TAG, "Find provider for accessoryRecord");
            for (UsbRegistrationIdentification accessoryProvider : accessoryRecord.getRegistrations()) {
                String primaryUUID = UUID.randomUUID().toString();
                Accessory accessory = new Accessory(accessoryProvider);

                String agentUuid = (accessoryRecord.getAgentId() != null) ? accessoryRecord.getAgentId().toString() : null;
                String name = (accessoryRecord.getName() != null) ? accessoryRecord.getName().toString() : null;
                String description = (accessoryRecord.getLocalizedDescription() != null && accessoryRecord.getLocalizedDescription().getStringId() != null) ?
                        accessoryRecord.getLocalizedDescription().getStringId().getValue() : null;

                String rawMetadata = getRawMetadata(solutionJson, accessoryRecord.getTypeGUN(), agentUuid);
                if (rawMetadata == null) rawMetadata = JsonHelper.toJson(accessoryRecord);

                ContentValues pcv = new ContentValuesBuilder()
                        .setSolutionId(solutionId)
                        .setAgentID(agentUuid)
                        .setPackageName(packageName)
                        .setFunctionType(accessoryRecord.getTypeGUN())
                        .setMetadata(rawMetadata)
                        .setAgentName(name)
                        .setAgentDescription(description)
                        .setExtData1(JsonHelper.toJson(accessory))

                        .build();
                DatabaseHelper.upsertProvider(primaryUUID, pcv);
            }
        }

        // Device Usage
        DeviceUsageAgentRegistrationRecord deviceUsageRecord =
                SolutionManifestHelper.getDeviceUsageRecord(solutionContent.getRegistrations());
        if (deviceUsageRecord != null) {
            Log.i(TAG, "Find provider for deviceUsageRecord");

            String agentUuid = (deviceUsageRecord.getAgentId() != null) ? deviceUsageRecord.getAgentId().toString() : null;
            String name = (deviceUsageRecord.getName() != null) ? deviceUsageRecord.getName().toString() : null;
            String description = (deviceUsageRecord.getLocalizedDescription() != null && deviceUsageRecord.getLocalizedDescription().getStringId() != null) ?
                    deviceUsageRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, deviceUsageRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(deviceUsageRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(deviceUsageRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(deviceUsageRecord.getAgentId().toString(), pcv);
        }

        // Supplies
        SuppliesAgentRegistrationRecord suppliesRecord =
                SolutionManifestHelper.getSuppliesRecord(solutionContent.getRegistrations());
        if (suppliesRecord != null) {
            Log.i(TAG, "Find provider for suppliesRecord");

            String agentUuid = (suppliesRecord.getAgentId() != null) ? suppliesRecord.getAgentId().toString() : null;
            String name = (suppliesRecord.getName() != null) ? suppliesRecord.getName().toString() : null;
            String description = (suppliesRecord.getLocalizedDescription() != null && suppliesRecord.getLocalizedDescription().getStringId() != null) ?
                    suppliesRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, suppliesRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(suppliesRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(suppliesRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(suppliesRecord.getAgentId().toString(), pcv);
        }

        SecurityAgentRegistrationRecord securityRecord =
                SolutionManifestHelper.getSecurityRecord(solutionContent.getRegistrations());
        if (securityRecord != null) {
            Log.i(TAG, "Find provider for securityRecord");

            String agentUuid = (securityRecord.getAgentId() != null) ? securityRecord.getAgentId().toString() : null;
            String name = (securityRecord.getName() != null) ? securityRecord.getName().toString() : null;
            String description = (securityRecord.getLocalizedDescription() != null && securityRecord.getLocalizedDescription().getStringId() != null) ?
                    securityRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, securityRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(securityRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(securityRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(securityRecord.getAgentId().toString(), pcv);
        }

        SolutionNotificationAgentRegistrationRecord solutionNotificationRecord =
                SolutionManifestHelper.getSolutionNotificationRecord(solutionContent.getRegistrations());
        if (solutionNotificationRecord != null) {
            Log.i(TAG, "Find provider for solutionNotificationRecord");

            String agentUuid = (solutionNotificationRecord.getAgentId() != null) ? solutionNotificationRecord.getAgentId().toString() : null;
            String name = (solutionNotificationRecord.getName() != null) ? solutionNotificationRecord.getName().toString() : null;
            String description = (solutionNotificationRecord.getLocalizedDescription() != null && solutionNotificationRecord.getLocalizedDescription().getStringId() != null) ?
                    solutionNotificationRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, solutionNotificationRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(solutionNotificationRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(solutionNotificationRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(solutionNotificationRecord.getAgentId().toString(), pcv);
        }


        JobStatisticsAgentRegistrationRecord jobStatisticsRecord =
                SolutionManifestHelper.getJobStatisticsRecord(solutionContent.getRegistrations());
        if (jobStatisticsRecord != null) {
            Log.i(TAG, "Find provider for jobStatisticsAgentRegistrationRecord");

            String agentUuid = (jobStatisticsRecord.getAgentId() != null) ? jobStatisticsRecord.getAgentId().toString() : null;
            String name = (jobStatisticsRecord.getName() != null) ? jobStatisticsRecord.getName().toString() : null;
            String description = (jobStatisticsRecord.getLocalizedDescription() != null && jobStatisticsRecord.getLocalizedDescription().getStringId() != null) ?
                    jobStatisticsRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, jobStatisticsRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(jobStatisticsRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(jobStatisticsRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(jobStatisticsRecord.getAgentId().toString(), pcv);
        }

        MessageCenterAgentRegistrationRecord messageCenterRecord =
                SolutionManifestHelper.getMessageCenterRecord(solutionContent.getRegistrations());
        if (messageCenterRecord != null) {
            Log.i(TAG, "Find provider for MessageCenterAgentRegistrationRecord");

            String agentUuid = (messageCenterRecord.getAgentId() != null) ? messageCenterRecord.getAgentId().toString() : null;
            String name = (messageCenterRecord.getName() != null) ? messageCenterRecord.getName().toString() : null;
            String description = (messageCenterRecord.getLocalizedDescription() != null && messageCenterRecord.getLocalizedDescription().getStringId() != null) ?
                    messageCenterRecord.getLocalizedDescription().getStringId().getValue() : null;

            String rawMetadata = getRawMetadata(solutionJson, messageCenterRecord.getTypeGUN(), agentUuid);
            if (rawMetadata == null) rawMetadata = JsonHelper.toJson(messageCenterRecord);

            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(solutionId)
                    .setAgentID(agentUuid)
                    .setPackageName(packageName)
                    .setFunctionType(messageCenterRecord.getTypeGUN())
                    .setMetadata(rawMetadata)
                    .setAgentName(name)
                    .setAgentDescription(description)

                    .build();
            DatabaseHelper.upsertProvider(messageCenterRecord.getAgentId().toString(), pcv);
        }

        // TODO need to handle about other features (webservices)

//
//        List<Provider> webservicesProviders = InstallationHelper.getProviderWithTarget(connector, Constants.PROVIDER_TYPE_WEBSERVICES);
//        if (webservicesProviders != null && webservicesProviders.size() > 0) {
//            //WEBSERVICES provider, UUID is mandatory.
//            Log.i(TAG, "Find provider for WEBSERVICES");
//            for (Provider webservicesProvider : webservicesProviders) {
//                if (webservicesProvider.getTitle() != null
//                        && webservicesProvider.getDescription() != null) {
//
//                    ContentValues pcv = new ContentValues();
//                    pcv.put(PackageContract.PackageProviderEntry.UUID, webservicesProvider.getUuid());
//                    pcv.put(PackageContract.PackageProviderEntry.PARENT_UUID, uuid);
//                    pcv.put(PackageContract.PackageProviderEntry.SOLUTION_UUID, solutionUuid);
//                    pcv.put(PackageContract.PackageProviderEntry.INSTALL_DATE, System.currentTimeMillis());
//                    pcv.put(PackageContract.PackageProviderEntry.FUNCTION_TYPE, Constants.PROVIDER_TYPE_WEBSERVICES);
//                    pcv.put(PackageContract.PackageProviderEntry.METADATA,
//                            JsonHelper.toJson(webservicesProvider.getEndPoints()));
//                    pcv.put(PackageContract.PackageProviderEntry.NAME, JsonHelper.toJson(webservicesProvider.getTitle()));
//                    pcv.put(PackageContract.PackageProviderEntry.DESCRIPTION, JsonHelper.toJson(webservicesProvider.getDescription()));
//                    InstallationHelper.createOrUpdateProvider(context, webservicesProvider.getUuid(), pcv, authorization);
//                }
//            }
//        }
    }

    private void updateAttestation(String hash, String solutionId) {
        if (!TextUtils.isEmpty(hash)) {
            ContentValues pcv = new ContentValues();
            pcv.put(PackageContract.PackageAttestationEntry.SOLUTION_ID, solutionId);
            pcv.put(PackageContract.PackageAttestationEntry.DATA, hash);
            DatabaseHelper.upsertAttestation(solutionId, pcv);
        }
    }

    private void updateInstallerDatabase(SolutionContent solutionContent, String packageName) throws Exception {
        Log.i(TAG, "update installer databases: " + packageName);
        ContentValues cv = DatabaseHelper.buildInstallerValues(solutionContent, packageName, PackageInstallerState.isCompleted, null);
        DatabaseHelper.upsertInstaller(cv);
    }

    private void updatePackagesDatabase(SolutionContent solutionContent, ApplicationAgentRegistrationRecord appRecord, ApkInfo apkInfo, String solutionJson, String allowListType) {
        ContentValues cv = DatabaseHelper.buildPackageValues(apkInfo, solutionContent, appRecord);
        Log.i(TAG, "updatePackagesDatabase 1");

        cv.put(PackageContract.PackageEntry.METADATA, solutionJson);
        if (allowListType != null) {
            cv.put(PackageContract.PackageEntry.ALLOWLIST_TYPE, allowListType);
        }
        if (SolutionManifestHelper.isMainRecord(appRecord)) {
            Intent launchIntent = getMainLaunchIntent(packageName);
            if (launchIntent != null) {
                cv.put(PackageContract.PackageEntry.LAUNCH_INTENT, launchIntent.toUri(URI_INTENT_SCHEME));
            }
            cv.put(PackageContract.PackageEntry.IS_MAIN_ACTIVITY, true);
        } else {
            cv.put(PackageContract.PackageEntry.LAUNCH_INTENT,
                    getSubLaunchIntent(packageName, appRecord.getTarget().getWorkpathApplicationTarget().getPackage().toString()).toUri(URI_INTENT_SCHEME));
            cv.put(PackageContract.PackageEntry.IS_MAIN_ACTIVITY, false);
        }
        String solutionId = solutionContent.getSolutionId().toString();
        String agentId = appRecord.getAgentId().toString();
        DatabaseHelper.upsertPackage(solutionId, agentId, cv);
    }

    private void updatePackagesDatabase(SolutionContent solutionContent, ApkInfo apkInfo, String solutionJson, String allowListType) {
        ContentValues cv = DatabaseHelper.buildPackageValues(packageName, solutionContent);
        cv.put(PackageContract.PackageEntry.IS_MAIN_ACTIVITY, false);
        cv.put(PackageContract.PackageEntry.APK_VERSION, apkInfo.getVersionName());
        cv.put(PackageContract.PackageEntry.METADATA, solutionJson);
        if (allowListType != null) {
            cv.put(PackageContract.PackageEntry.ALLOWLIST_TYPE, allowListType);
        }
        DatabaseHelper.upsertPackage(solutionContent.getSolutionId().toString(), null, cv);
    }

    private Intent getMainLaunchIntent(String packageName) {
        Log.i(TAG, "getLaunchIntent : " + packageName);
        Context context = PackageManagerApplication.getAppContext();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            Log.i(TAG, "launchIntent : " + launchIntent);
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | FLAG_ACTIVITY_CLEAR_TASK);
            intent.setPackage(packageName);
            intent.setClassName(packageName, launchIntent.getComponent().getClassName());
            return intent;
        } else {
            return null;
        }
    }

    private Intent getSubLaunchIntent(String packageName, String workpathTargetPackage) {
        String activityName = "";
        if (workpathTargetPackage.contains("/")) {
            // remove package part before /
            String[] parts = workpathTargetPackage.split("/");
            if (parts.length > 1) {
                activityName = parts[1];
            }
        }

        Intent sample = new Intent();
        sample.addCategory(Intent.CATEGORY_LAUNCHER);
        sample.setAction(Intent.ACTION_MAIN);
        sample.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | FLAG_ACTIVITY_CLEAR_TASK);
        sample.setPackage(packageName);
        sample.setClassName(packageName, activityName);
        return sample;
    }

    protected int uninstallProviders(String solutionId) {
        Context context = PackageManagerApplication.getAppContext();
        Cursor cursor = null;
        int providerDeleted = 0;

        try {
            String querySelection = PackageContract.PackageProviderEntry.SOLUTION_ID + " = ?";
            String[] querySelectionArgs = new String[]{solutionId};

            cursor = context.getContentResolver().query(
                    PackageContract.PROVIDERS_CONTENT_URI, null, querySelection, querySelectionArgs, null);
            if (cursor != null) {
                String agentId, functionType, accessoryParam;
                while (cursor.moveToNext()) {
                    agentId = cursor.getString(cursor.getColumnIndexOrThrow(PackageContract.PackageProviderEntry.AGENT_ID));
                    functionType = cursor.getString(cursor.getColumnIndexOrThrow(PackageContract.PackageProviderEntry.FUNCTION_TYPE));
                    querySelection = PackageContract.PackageProviderEntry.SOLUTION_ID + " = ? and "
                            + PackageContract.PackageProviderEntry.AGENT_ID + " = ? and "
                            + PackageContract.PackageProviderEntry.FUNCTION_TYPE + " = ?";
                    querySelectionArgs = new String[]{solutionId, agentId, functionType};
                    providerDeleted += context.getContentResolver().delete(PackageContract.PROVIDERS_CONTENT_URI, querySelection, querySelectionArgs);
                }
                Log.d(TAG, "Package provider delete result = " + (providerDeleted > 0) + ", and deleted items " + providerDeleted + ": " + solutionId);
            }
        } catch (Exception e) {
            Log.e(TAG, "uninstallProviders has an error in " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return providerDeleted;
    }

    private Intent createLaunchIntent(ComponentName componentName) {
        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.setComponent(componentName);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        return launchIntent;
    }

    /**
     * get activity name from workpathTargetPackage (packageName/ActivityName) in registration record
     *
     * @param workpathTargetPackage packageName/ActivityName (ex: "com.hp.workpath.sample.authentication/.AuthenticationActivity")
     * @return activity name
     */
    private String getActivityName(String workpathTargetPackage) {
        String activityName = "";
        if (workpathTargetPackage.contains("/")) {
            // remove package part before /
            String[] parts = workpathTargetPackage.split("/");
            if (parts.length > 1) {
                activityName = parts[1];
            }
        }
        return activityName;
    }
}
