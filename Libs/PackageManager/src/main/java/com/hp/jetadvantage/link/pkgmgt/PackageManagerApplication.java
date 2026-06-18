package com.hp.jetadvantage.link.pkgmgt;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.hp.jetadvantage.link.pkgmgt.activities.PermissionActivity;
import com.hp.jetadvantage.link.pkgmgt.helper.EventReceiverManager;
import com.hp.jetadvantage.link.pkgmgt.receivers.AttestationChangeBroadcastReceiver;

import java.lang.ref.WeakReference;

public class PackageManagerApplication extends MultiDexApplication {
    private static final String TAG = "PacM";
    private static WeakReference<Context> contextRef;

    public void onCreate() {
        super.onCreate();
        PackageManagerApplication.contextRef = new WeakReference<>(getApplicationContext());

        //TODO if we remove this registerReceiver, adb shell commend should be changed to like this
        // adb -s 15.26.148.128 shell am broadcast -n "com.hp.jetadvantage.link.packagemanager/com.hp.jetadvantage.link.pkgmgt.receivers.AttestationChangeBroadcastReceiver" -a com.hp.jetadvantage.link.intent.action.ATTESTATION --es UUID 11111111-1111-1111-9996-111111111111 --es EXTRA_DATA '{"test":"test1234"}' --es EXTRA_USER 'wuseok' --es EXTRA_LDBKEY 'sdk1234'
        IntentFilter filter = new IntentFilter("com.hp.jetadvantage.link.intent.action.ATTESTATION");
        this.registerReceiver(new AttestationChangeBroadcastReceiver(), filter);

        // Fallback initialization: some devices may not deliver PACKAGE_ADDED to Pacman itself.
        // Rebuild once at app startup so eventreceivers rows/logs are still populated.
        try {
            EventReceiverManager eventReceiverManager = new EventReceiverManager(getApplicationContext());
            int rows = eventReceiverManager.scanInstalledPackagesAndRebuildTable();
            Log.i(TAG, "[EVENT-TABLE] Startup rebuild completed. rows=" + rows);
        } catch (Exception e) {
            Log.w(TAG, "[EVENT-TABLE] Startup rebuild failed: " + e.getMessage());
        }

        try {
            Log.i(TAG, "Request permission");
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, "Request permission failed");
        }
    }

    public static Context getAppContext() {
        return contextRef.get();
    }
}
