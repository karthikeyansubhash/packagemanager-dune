package com.hp.jetadvantage.link.pkgmgt.services;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.URI_INTENT_SCHEME;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.builder.ContentValuesBuilder;
import com.hp.jetadvantage.link.pkgmgt.helper.DatabaseHelper;
import com.hp.jetadvantage.link.pkgmgt.helper.PackageHelper;
import com.hp.jetadvantage.link.pkgmgt.model.BuiltinApp;
import com.hp.jetadvantage.link.pkgmgt.model.BuiltinAppsContainer;
import com.hp.jetadvantage.link.pkgmgt.notification.ServiceNotification;
import com.hp.jetadvantage.link.pkgmgt.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BootTasksService extends Service {

    private static final String TAG = Constants.TAG + "BootTasksService";
    private static final String BOOT_TASKS_PREFS = "BootTasksPrefs";
    private static final String KEY_BUILTIN_VERSION = "builtinAppsVersion";
    private static final String JSON_BUILT_IN_APPS = "builtin/builtin_apps.json";
    private static final String TYPE_BUILT_IN_APPS = "BuiltInApp";

    private final IBinder binder = new LocalBinder();

    private HandlerThread handlerThread;

    public class LocalBinder extends Binder {
        public BootTasksService getService() {
            return BootTasksService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BootTasksService started");

        ServiceNotification.showNotification(this);

        handlerThread = new HandlerThread("BootTasksThread");
        handlerThread.start();
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        backgroundHandler.post(() -> {
            runOneTimeSetup();
            runEveryBootTasks();
            Log.d(TAG, "BootTasksService finished");
            stopSelf();
        });

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void runOneTimeSetup() {
        SharedPreferences prefs = getSharedPreferences(BOOT_TASKS_PREFS, Context.MODE_PRIVATE);
        String savedVersion = prefs.getString(KEY_BUILTIN_VERSION, null);
        BuiltinAppsContainer container;
        try {
            container = loadBuiltinAppsContainer();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load builtin apps JSON for version check", e);
            return; // abort setup
        }
        String currentVersion = container.getVersion();
        boolean shouldSetup = savedVersion == null || !savedVersion.equals(currentVersion);

        if (shouldSetup) {
            Log.d(TAG, "Version changed or first run (" + savedVersion + " -> " + currentVersion + "), performing setup.");
            updateBuiltinAppsDb(container.getBuiltinApps());

            prefs.edit().putString(KEY_BUILTIN_VERSION, currentVersion).apply();
            Log.d(TAG, "One-time setup complete.");
        } else {
            Log.d(TAG, "Skipping setup, version unchanged.");
        }
    }

    private void runEveryBootTasks() {
        Log.d(TAG, "Performing tasks for every boot.");
        Context context = getApplicationContext();

        // Process incomplete installers
        PackageHelper.processIncompleteInstallers(context);

        // Cleanup expired items and temporary files
        PackageHelper.cleanupExpiredInstallers(context);
        PackageHelper.cleanTemporaryFolder(Constants.TEMPORARY_FOLDER);

        Log.d(TAG, "Every boot tasks complete.");
    }

    /**
     * Loads the builtin_apps.json asset into a container model.
     */
    private BuiltinAppsContainer loadBuiltinAppsContainer() throws IOException {
        try (InputStream is = getAssets().open(JSON_BUILT_IN_APPS);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            BuiltinAppsContainer container = new Gson().fromJson(reader, BuiltinAppsContainer.class);
            if (container == null) {
                throw new IOException("Failed to parse builtin apps JSON: container is null");
            }
            return container;
        }
    }

    /**
     * Inserts or updates each built-in app into the DB.
     */
    private void updateBuiltinAppsDb(List<BuiltinApp> apps) {
        for (BuiltinApp app : apps) {
            Intent launchIntent = Utils.createLaunchIntent(
                    new ComponentName(app.getPackageName(), app.getActivityName()));
            ContentValues pcv = new ContentValuesBuilder()
                    .setSolutionId(app.getAgentId())
                    .setAgentID(app.getAgentId())
                    .setPackageName(app.getPackageName())
                    .setFunctionType(TYPE_BUILT_IN_APPS)
                    .setLaunchIntent(launchIntent.toUri(URI_INTENT_SCHEME))
                    .setAgentName(app.getName())
                    .build();
            DatabaseHelper.upsertProvider(app.getAgentId(), pcv);
        }
    }
}
