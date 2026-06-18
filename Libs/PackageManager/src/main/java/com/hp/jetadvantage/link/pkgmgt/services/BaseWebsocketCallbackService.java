package com.hp.jetadvantage.link.pkgmgt.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.system.IWebsocketCallback;
import com.hp.jetadvantage.link.system.IWebsocketCallbackService;


public abstract class BaseWebsocketCallbackService extends Service {

    public static final String TAG = Constants.TAG + "Websocket";

    protected IWebsocketCallbackService callbackService = null;

    private String mTarget = "";
    private IWebsocketCallback callback = new IWebsocketCallback.Stub() {
        @Override
        public void onMessageReceived(int what, String data) throws RemoteException {
            onReceived(what, data);
        }
    };

    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected .. : ");
            callbackService = IWebsocketCallbackService.Stub.asInterface(service);
            try {
                callbackService.addCallback(callback, mTarget);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected .. : ");
            callbackService = null;
        }
    };

    public BaseWebsocketCallbackService() {
    }

    public BaseWebsocketCallbackService(String target) {
        this.mTarget = target;
    }

    public abstract void onReceived(int what, String data);

    public void sendMessage(int what, String data) throws RemoteException {
        callbackService.sendMessage(what, data);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean result = false;
        Intent i = new Intent();
        i.setAction("com.hp.jetadvantage.link.system.services.WebSocketCallbackService");
        i.setPackage("com.hp.jetadvantage.link.system");
        try {
            result = bindService(i, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand() bindService Exception:" + e.getMessage());
        }

        Log.i(TAG, "onStartCommand() bindService result=" + result);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            unbindService(connection);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy() unbindService Exception:" + e.getMessage());
        }
    }
}
