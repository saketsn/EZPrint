package com.example.ezprint;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.RegisterDeviceRequest;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends BaseActivity {

    private DrawerLayout drawerLayout;
    private ImageView headerAvatar, navDrawerAvatar;
    private TextView userNameText, navDrawerShopName, navDrawerOwnerName;
    private NavigationView navView;

    private final ActivityResultLauncher<Intent> profileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    loadUserData();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        headerAvatar = findViewById(R.id.user_avatar);
        userNameText = findViewById(R.id.user_name_text);
        headerAvatar.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // --- CORRECTED Window Insets Listener for Bottom Nav Bar ---
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);

        setupBottomNavigation();
        setupSideNavigation();
        loadUserData();
        checkAndSendFcmToken();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            navView.setCheckedItem(R.id.drawer_dashboard);
        }
    }

    private void setupBottomNavigation(){
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_activity) {
                openFragment(new DashboardFragment());
                return true;
            } else if (id == R.id.nav_search) {
                openFragment(new SearchOrder());
                return true;
            } else if (id == R.id.nav_qr_code) {
                openFragment(new QrCode());
                return true;
            } else if (id == R.id.nav_price_settings) {
                startActivity(new Intent(Home.this, PriceSettingsActivity.class));
                return true;
            }
            return false;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> Toast.makeText(this, "Add Transaction Clicked!", Toast.LENGTH_SHORT).show());
    }

    private void setupSideNavigation() {
        navView = findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        navDrawerAvatar = headerView.findViewById(R.id.nav_header_avatar);
        navDrawerShopName = headerView.findViewById(R.id.nav_header_shop_name);
        navDrawerOwnerName = headerView.findViewById(R.id.nav_header_owner_name);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.drawer_dashboard) {
                openFragment(new DashboardFragment());
            } else if (id == R.id.drawer_profile) {
                profileLauncher.launch(new Intent(Home.this, Profile.class));
            } else if (id == R.id.drawer_logout) {
                showLogoutDialog();
            } else if (id == R.id.drawer_settings) {
                startActivity(new Intent(Home.this, SettingsActivity.class));
            } else if (id == R.id.drawer_orders) {
                startActivity(new Intent(Home.this, AllOrders.class));
            } else if (id == R.id.drawer_pending_orders) {
                startActivity(new Intent(Home.this, PendingOrders.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadUserData(){
        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE);
        String shopName = prefs.getString("SHOP_NAME", "Shop Name");
        String ownerName = prefs.getString("OWNER_NAME", "Owner Name");
        String profileImageUrl = prefs.getString("PROFILE_PIC", null);

        userNameText.setText(shopName);
        navDrawerShopName.setText(shopName);
        navDrawerOwnerName.setText(ownerName);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            String fullUrl = RetrofitClient.BASE_URL + profileImageUrl;
            Glide.with(this).load(fullUrl).circleCrop().placeholder(R.drawable.ic_avatar_placeholder).into(headerAvatar);
            Glide.with(this).load(fullUrl).circleCrop().placeholder(R.drawable.ic_avatar_placeholder).into(navDrawerAvatar);
        } else {
            headerAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            navDrawerAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void performLogout() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging out...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String fcmToken = task.getResult();
                if (shopId != -1) {
                    unregisterDeviceFromServer(shopId, fcmToken, progressDialog);
                } else {
                    progressDialog.dismiss();
                    finalizeLogout();
                }
            } else {
                progressDialog.dismiss();
                Log.w("Logout", "Fetching FCM token failed during logout.", task.getException());
                finalizeLogout();
            }
        });
    }

    private void unregisterDeviceFromServer(int shopId, String fcmToken, ProgressDialog progressDialog) {
        ApiService apiService = RetrofitClient.getApiService();
        RegisterDeviceRequest request = new RegisterDeviceRequest(shopId, fcmToken);
        apiService.unregisterDevice(request).enqueue(new Callback<ApiErrorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                if(response.isSuccessful()) {
                    Log.d("Logout", "Device successfully unregistered from server.");
                } else {
                    Log.e("Logout", "Failed to unregister device from server.");
                }
                progressDialog.dismiss();
                finalizeLogout();
            }

            @Override
            public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                Log.e("Logout", "Network error while unregistering device.", t);
                progressDialog.dismiss();
                finalizeLogout();
            }
        });
    }

    private void finalizeLogout() {
        FirebaseMessaging.getInstance().deleteToken();
        getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE).edit().clear().apply();

        Intent intent = new Intent(Home.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkAndSendFcmToken() {
        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);
        String storedToken = prefs.getString("FCM_TOKEN", null);

        if (shopId == -1) return;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String currentToken = task.getResult();
                if (storedToken == null || !currentToken.equals(storedToken)) {
                    Log.d("FCM_TOKEN", "Token has changed. Updating server.");
                    sendTokenToServer(shopId, currentToken);
                } else {
                    Log.d("FCM_TOKEN", "Token is up to date.");
                }
            }
        });
    }

    private void sendTokenToServer(int shopId, String fcmToken) {
        ApiService apiService = RetrofitClient.getApiService();
        RegisterDeviceRequest request = new RegisterDeviceRequest(shopId, fcmToken);
        apiService.registerDevice(request).enqueue(new Callback<ApiErrorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM_TOKEN", "Token successfully updated on server.");
                    SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE);
                    prefs.edit().putString("FCM_TOKEN", fcmToken).apply();
                } else {
                    Log.e("FCM_TOKEN", "Failed to send token to server. Code: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                Log.e("FCM_TOKEN", "Network error while sending token: ", t);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

