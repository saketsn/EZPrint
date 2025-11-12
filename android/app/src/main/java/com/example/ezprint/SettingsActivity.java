package com.example.ezprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    private SwitchMaterial darkModeSwitch, inAppNotificationsSwitch, pushNotificationsSwitch;
    private SharedPreferences settingsPrefs;
    private ImageView homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Handle edge-to-edge display insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use a dedicated SharedPreferences file just for settings
        settingsPrefs = getSharedPreferences("EZPrint_Settings", MODE_PRIVATE);

        initializeViews();
        loadSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        inAppNotificationsSwitch = findViewById(R.id.in_app_notifications_switch);
        pushNotificationsSwitch = findViewById(R.id.push_notifications_switch);
        homeIcon = findViewById(R.id.home_icon);
    }

    /**
     * Loads the saved settings from SharedPreferences and updates the UI.
     */
    private void loadSettings() {
        // Load the saved state for each switch. Default values are provided
        // for the first time the app is run.
        darkModeSwitch.setChecked(settingsPrefs.getBoolean("dark_mode_enabled", false));
        inAppNotificationsSwitch.setChecked(settingsPrefs.getBoolean("in_app_notifications_enabled", true));
        pushNotificationsSwitch.setChecked(settingsPrefs.getBoolean("push_notifications_enabled", true));
    }

    private void setupClickListeners() {
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> finish());

        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, Home.class));
            finish();
        });

        // Dark Mode Switch Listener
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new preference
            settingsPrefs.edit().putBoolean("dark_mode_enabled", isChecked).apply();

            // Apply the theme change immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // In-App (Snackbar) Notifications Switch Listener
        inAppNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("in_app_notifications_enabled", isChecked).apply();
        });

        // Push (System) Notifications Switch Listener
        pushNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("push_notifications_enabled", isChecked).apply();
        });
    }
}

