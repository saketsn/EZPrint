package com.example.ezprint;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String ACTION_NEW_ORDER = "com.example.ezprint.NEW_ORDER_RECEIVED";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "New Order";
        String body = "You have a new order waiting!";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // The BaseActivity will decide how to show the in-app notification
        // based on the user's settings.
        if (MyApp.isAppInForeground()) {
            Intent intent = new Intent(ACTION_NEW_ORDER);
            intent.putExtra("orderId", remoteMessage.getData().get("orderId"));
            intent.putExtra("customerName", remoteMessage.getData().get("customerName"));
            intent.putExtra("fileCount", remoteMessage.getData().get("fileCount"));
            intent.putExtra("totalCopies", remoteMessage.getData().get("totalCopies"));
            intent.putExtra("printType", remoteMessage.getData().get("printType"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            // If the app is in the background, check the user's preference
            SharedPreferences settingsPrefs = getSharedPreferences("EZPrint_Settings", MODE_PRIVATE);
            boolean pushNotificationsEnabled = settingsPrefs.getBoolean("push_notifications_enabled", true);

            if (pushNotificationsEnabled) {
                sendSystemNotification(title, body);
            }
        }
    }

    /**
     * Creates and displays a system notification that, when tapped, takes the user
     * directly to the PendingOrders activity with a proper back stack.
     * @param title The title of the notification.
     * @param body The main text of the notification.
     */
    private void sendSystemNotification(String title, String body) {
        // Create an Intent that will launch the MainActivity (your splash screen)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // This "extra" is the signal that tells MainActivity to navigate to PendingOrders
        intent.putExtra("NAVIGATE_TO_PENDING_ORDERS", true);

        // TaskStackBuilder ensures that when the user presses back from PendingOrders,
        // they go to the Home screen, not out of the app.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "default_channel_id";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Since Android Oreo, a notification channel is required.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Order Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "Refreshed token: " + token);
        // When a token is refreshed, you should send it to your server if the user is logged in.
        // The checkAndSendFcmToken() method in Home.java handles this on app start.
    }
}