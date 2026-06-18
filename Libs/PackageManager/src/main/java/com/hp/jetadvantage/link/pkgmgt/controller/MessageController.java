package com.hp.jetadvantage.link.pkgmgt.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;
import com.hp.jetadvantage.link.pkgmgt.services.DuneCallbackService;

public abstract class MessageController {

    private static final String TAG = Constants.TAG + "MessageController";
    private DuneCallbackService duneService;

    protected void onStart() {
        Log.i(TAG, "BindService: onStart");
        Intent intent = new Intent(PackageManagerApplication.getAppContext(), DuneCallbackService.class);
        PackageManagerApplication.getAppContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    protected void onStop() {
        try {
            PackageManagerApplication.getAppContext().unbindService(connection);
        } catch (Exception e) {
            Log.e(TAG, "Error while unbinding service: " + e.getMessage());
        }
    }

    protected abstract void onServiceConnected();

    protected void sendMessage(int responseCode, String data) {
        duneService.sendMessageToWebSocket(responseCode, data);
        onStop();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "BindService: onServiceConnected");
            DuneCallbackService.DuneServiceBinder binder = (DuneCallbackService.DuneServiceBinder) service;
            duneService = binder.getService();
            MessageController.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "BindService: onServiceDisconnected");
        }
    };
}
