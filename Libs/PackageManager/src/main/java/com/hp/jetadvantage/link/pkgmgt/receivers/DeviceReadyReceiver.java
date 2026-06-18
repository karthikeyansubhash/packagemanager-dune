package com.hp.jetadvantage.link.pkgmgt.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.connect.OXPdConnect;
import com.hp.jetadvantage.link.pkgmgt.services.DuneCallbackService;
import com.hp.jetadvantage.link.pkgmgt.services.BootTasksService;

public class DeviceReadyReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.TAG + "DeviceReadyReceiver";
    public static final String DEVICE_IP = "device_ip";
    public static final String DEVICE_TOKEN = "device_token";
    private static final String DEVICE_READY_ACTION = "com.hp.workpath.system.DEVICE_READY";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive : ENTER :");
        if (context == null || intent == null || intent.getAction() == null || !DEVICE_READY_ACTION.equals(intent.getAction())) {
            return;
        }

        String ip = intent.getStringExtra(DEVICE_IP);
        String token = intent.getStringExtra(DEVICE_TOKEN);
        Log.i(TAG, "onReceive : ip : " + ip);
        if (token == null || token.isEmpty()) {
            Log.i(TAG, "onReceive : empty token");
        }
        Log.d(TAG, "onReceive : token : " + token);

        // Set the IP and token to the OXPdConnect
        // IP : The IP address of the device (HP Printer: 156.152.79.233)
        //      The IP address of the simulator (HP Printer: simulator IP address)
        OXPdConnect.getInstance().setIp(ip);
        OXPdConnect.getInstance().setToken(token);

        // Start BootTasksService to handle initialization in the background.
        Intent serviceIntent = new Intent(context, BootTasksService.class);
        ContextCompat.startForegroundService(context, serviceIntent);

        if (!DuneCallbackService.isServiceRunning()) {
            DuneCallbackService.start(context);
        }
    }
}
