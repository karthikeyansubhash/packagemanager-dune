package com.hp.jetadvantage.link.pkgmgt.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.hp.jetadvantage.link.pkgmgt.R;

public class ServiceNotification {
    private static final int NOTIFY_SERVICE_STATE = 1820;
    private static final String CHANNEL_ID = "com.hp.jetadvantage.link.pkgmgt";
    private static final String CHANNEL_NAME = "workpath pkg service is running.";

    public static void showNotification(Service service){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                closeNotification(service);
            } catch (Throwable t) {
            }

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(service, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_transparent)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .build();

            service.startForeground(NOTIFY_SERVICE_STATE, notification);
        }
    }

    public static void closeNotification(Service service){
        NotificationManager mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFY_SERVICE_STATE);
    }
}