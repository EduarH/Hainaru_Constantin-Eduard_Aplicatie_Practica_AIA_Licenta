package com.buddy.buddy.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.buddy.buddy.R;
import com.buddy.buddy.receivers.AcceptReceiver;
import com.buddy.buddy.receivers.CancelReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessagingClient extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FirebaseMessagingClient", "TOKEN: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        RemoteMessage.Notification notification = message.getNotification();
        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");

        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("service request")) {
                    showNotificationApiOreoActions(title, body, data.get("idClient"));
                } else {
                    showNotificationApiOreo(title, body);
                }
            } else {
                if (title.contains("service request")) {
                    showNotificationActions(title, body, data.get("idClient"));
                } else {
                    showNotification(title, body);
                }
            }
        }

    }

    private void showNotification(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), getPendingFlags());
        Uri soud = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, intent, soud);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), getPendingFlags());
        Uri soud = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, soud);
        notificationHelper.getManager().notify(1, builder.build());
    }

    private int getPendingFlags() {
        int pendingFlags;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return pendingFlags;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoActions(String title, String body, String idClient) {

        // Accept
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, getPendingFlags());
        Notification.Action acceptAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Accept",
                pendingIntent
        ).build();

        // Cancel
        Intent canceltIntent = new Intent(this, CancelReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, canceltIntent, getPendingFlags());
        Notification.Action cancelAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancel",
                cancelPendingIntent
        ).build();

        Uri soud = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationActions(title, body, soud, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }

    private void showNotificationActions(String title, String body, String idClient) {

        // Accept
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, getPendingFlags());
        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Accept",
                pendingIntent
        ).build();

        // Cancel
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, getPendingFlags());
        NotificationCompat.Action canceltAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancel",
                cancelPendingIntent
        ).build();

        Uri soud = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApiActions(title, body, soud, acceptAction, canceltAction);
        notificationHelper.getManager().notify(2, builder.build());
    }
}
