package com.hp.jetadvantage.link.pkgmgt.services;

import static com.hp.jetadvantage.link.pkgmgt.helper.MessageHelper.WHO_AM_I;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.helper.MessageHelper;
import com.hp.jetadvantage.link.pkgmgt.notification.ServiceNotification;


public class DuneCallbackService extends BaseWebsocketCallbackService {

    private static final String TAG = Constants.TAG + "CallbackService";

    private final IBinder binder = new DuneServiceBinder();

    private static boolean isRunning = false;

    public DuneCallbackService() {
        super(WHO_AM_I);
        Log.i(TAG, "DuneCallbackService");
    }

    @Override
    public void sendMessage(int what, String data) throws RemoteException {
        Log.i(TAG, "sendMessage what: " + what + ", data: " + data);
        super.sendMessage(what, data);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        ServiceNotification.showNotification(this);
        Log.d(TAG, "onCreate WebSocketCallbackService svc.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void sendMessageToWebSocket(int msg, String data) {
        try {
            Log.i(TAG, "sendMessageToWebSocket: " + msg + ", data: " + data);
            sendMessage(msg, data);
        } catch (Exception ex) {
            try {
                Log.e(TAG, ex.getMessage());
                sendMessage(Activity.RESULT_CANCELED, "400 Error: " + ex.getMessage());
            } catch (RemoteException re) {
                Log.e(TAG, re.getMessage());
            }
        }
    }

    @Override
    public void onReceived(int what, String data) {
        Log.i(TAG, "onReceived what: " + what + ", data: " + data);
        try {
            MessageHelper duneMessageHelper = new MessageHelper();
            duneMessageHelper.parseMessage(getApplicationContext(), data);
        } catch (Exception ex) {
            sendMessageToWebSocket(Activity.RESULT_CANCELED, ex.getMessage());
            Log.e(TAG, ex.getMessage());
        }
    }

    public class DuneServiceBinder extends Binder {
        public DuneCallbackService getService() {
            return DuneCallbackService.this;
        }
    }

    public static void start(final Context context) {
        try {
            Log.i(TAG, "WebSocket Service start ");
            Intent intent = new Intent(context, DuneCallbackService.class);
            context.startForegroundService(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initial server(start) for unknown caller " + e.getMessage());
        }
    }

    public static boolean isServiceRunning() {
        return isRunning;
    }
}
