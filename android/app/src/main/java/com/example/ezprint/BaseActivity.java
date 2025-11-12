package com.example.ezprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A master activity that all other activities extend from.
 * It contains the global logic for handling real-time, in-app notifications
 * by checking user preferences and showing either a BottomSheet or a Snackbar.
 */
public abstract class BaseActivity extends AppCompatActivity implements NewOrderBottomSheet.OnDismissListener {

    private final Queue<Bundle> notificationQueue = new LinkedList<>();
    private boolean isBottomSheetVisible = false;

    private final BroadcastReceiver newOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 1. Check the user's saved preference for in-app notifications.
            SharedPreferences settingsPrefs = getSharedPreferences("EZPrint_Settings", MODE_PRIVATE);
            boolean inAppNotificationsEnabled = settingsPrefs.getBoolean("in_app_notifications_enabled", true);

            Bundle orderData = intent.getExtras();
            if (orderData == null) return;

            if (inAppNotificationsEnabled) {
                // 2. If the setting is ON, use the detailed BottomSheet with the queue system.
                notificationQueue.add(orderData);
                processNotificationQueue();
            } else {
                // 3. If the setting is OFF, show the simpler, less intrusive Snackbar immediately.
                String customerName = orderData.getString("customerName");
                showNewOrderSnackbar(customerName);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Register the receiver in every activity that extends this one
        LocalBroadcastManager.getInstance(this).registerReceiver(newOrderReceiver,
                new IntentFilter(MyFirebaseMessagingService.ACTION_NEW_ORDER));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver when the activity is not in the foreground
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newOrderReceiver);
    }

    /**
     * Checks the queue and displays the next notification as a BottomSheet if the UI is ready.
     */
    private void processNotificationQueue() {
        if (isBottomSheetVisible || notificationQueue.isEmpty()) {
            return;
        }
        Bundle orderData = notificationQueue.poll();
        if (orderData == null) return;

        isBottomSheetVisible = true;

        NewOrderBottomSheet bottomSheet = NewOrderBottomSheet.newInstance(
                orderData.getString("orderId"),
                orderData.getString("customerName"),
                orderData.getString("fileCount"),
                orderData.getString("totalCopies"),
                orderData.getString("printType")
        );
        bottomSheet.show(getSupportFragmentManager(), "NewOrderSheet");
    }

    /**
     * This method is called from the NewOrderBottomSheet via the interface when it is dismissed.
     */
    @Override
    public void onBottomSheetDismissed() {
        isBottomSheetVisible = false;
        // The sheet has closed, so immediately try to show the next notification in the queue.
        processNotificationQueue();
    }

    /**
     * Displays a simple Snackbar notification at the bottom of the current screen.
     * @param customerName The name of the customer who placed the order.
     */
    private void showNewOrderSnackbar(String customerName) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, "New order from " + customerName, Snackbar.LENGTH_LONG);
        snackbar.setAction("View", v -> {
            // When "View" is tapped, open the PendingOrders activity
            Intent intent = new Intent(this, PendingOrders.class);
            startActivity(intent);
        });
        snackbar.show();
    }
}