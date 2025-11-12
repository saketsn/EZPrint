package com.example.ezprint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the activity was launched from a notification tap
        boolean navigateToPending = getIntent().getBooleanExtra("NAVIGATE_TO_PENDING_ORDERS", false);

        if (navigateToPending) {
            // If launched from a notification, handle it immediately without the splash delay
            handleNotificationLaunch();
        } else {
            // Otherwise, proceed with the normal 3-second splash screen flow
            runNormalSplashFlow();
        }
    }

    private void handleNotificationLaunch() {
        SharedPreferences sharedPreferences = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false);

        Intent intent;
        if (isLoggedIn) {
            // If the user is logged in, go directly to the Pending Orders screen
            intent = new Intent(this, PendingOrders.class);
        } else {
            // If not logged in, they must log in first.
            intent = new Intent(this, Login.class);
        }
        startActivity(intent);
        finish(); // Always finish the splash activity immediately
    }

    private void runNormalSplashFlow() {
        // Delay for 3 seconds before deciding where to go
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(MainActivity.this, Home.class);
            } else {
                intent = new Intent(MainActivity.this, Login.class);
            }

            startActivity(intent);
            finish(); // Close splash screen
        }, 3000);
    }
}

