package com.example.ezprint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.LoginRequest;
import com.example.ezprint.models.LoginResponse;
import com.example.ezprint.models.RegisterDeviceRequest;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private TextInputEditText phoneEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView errorTextView;
    private ProgressBar loadingSpinner;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long requestStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        phoneEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        errorTextView = findViewById(R.id.error_message_text_view);
        loadingSpinner = findViewById(R.id.loading_spinner);
        TextView signUpView = findViewById(R.id.sign_up);
        signUpView.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());

        TextWatcher errorClearer = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneEditText.setError(null);
                passwordEditText.setError(null);
                errorTextView.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        phoneEditText.addTextChangedListener(errorClearer);
        passwordEditText.addTextChangedListener(errorClearer);
    }

    private void performLogin() {
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!isFormValid(phone, password)) {
            return;
        }

        requestStartTime = System.currentTimeMillis();
        showLoading(true);

        LoginRequest loginRequest = new LoginRequest(phone, password);
        ApiService apiService = RetrofitClient.getApiService();

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                long elapsedTime = System.currentTimeMillis() - requestStartTime;
                long delay = Math.max(0, 1000 - elapsedTime);

                handler.postDelayed(() -> {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        saveUserDataAndNavigate(response.body());
                    } else {
                        handleApiError(response);
                    }
                }, delay);
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                long elapsedTime = System.currentTimeMillis() - requestStartTime;
                long delay = Math.max(0, 1000 - elapsedTime);
                handler.postDelayed(() -> {
                    showLoading(false);
                    Log.e("LoginFailure", "Network error: ", t);
                    showError("Network error. Please check your connection.");
                }, delay);
            }
        });
    }

    private void saveUserDataAndNavigate(LoginResponse loginResponse) {
        SharedPreferences sharedPreferences = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("SHOP_ID", loginResponse.getShopId());
        editor.putString("SHOP_NAME", loginResponse.getShopName());
        editor.putString("OWNER_NAME", loginResponse.getOwnerName());
        editor.putString("PHONE", loginResponse.getPhone());
        editor.putString("ADDRESS", loginResponse.getAddress());
        editor.putString("EMAIL", loginResponse.getEmail());
        editor.putString("PROFILE_PIC", loginResponse.getProfileImg());
        editor.putString("QR_CODE_URL", loginResponse.getQrCodeUrl());
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.apply();

        // Get the FCM token and send it to the server in the background
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String fcmToken = task.getResult();
                Log.d("FCM_TOKEN", "Obtained token: " + fcmToken);
                sendTokenToServer(loginResponse.getShopId(), fcmToken);

                // Save token locally for future comparison
                SharedPreferences sharedPreferencesToken = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
                sharedPreferencesToken.edit().putString("FCM_TOKEN", fcmToken).apply();
            } else {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
            }
        });

        // Navigate to the Home screen
        Intent intent = new Intent(Login.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendTokenToServer(int shopId, String fcmToken) {
        ApiService apiService = RetrofitClient.getApiService();
        RegisterDeviceRequest request = new RegisterDeviceRequest(shopId, fcmToken);
        apiService.registerDevice(request).enqueue(new Callback<ApiErrorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM_TOKEN", "Token successfully sent to server.");
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

    private boolean isFormValid(String phone, String password) {
        boolean isValid = true;
        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            isValid = false;
        } else if (!phone.matches("\\d{10}")) {
            phoneEditText.setError("Please enter a valid 10-digit phone number");
            isValid = false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!password.matches("[a-zA-Z0-9]+")) {
            passwordEditText.setError("Password can only contain letters and numbers");
            isValid = false;
        }
        return isValid;
    }

    private void handleApiError(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                ApiErrorResponse errorResponse = new Gson().fromJson(errorBodyString, ApiErrorResponse.class);
                if (errorResponse != null && errorResponse.getError() != null) {
                    showError(errorResponse.getError());
                } else {
                    showError("An unknown error occurred.");
                }
            } catch (IOException | JsonSyntaxException e) {
                Log.e("ApiError", "Error parsing error body", e);
                showError("Error processing server response.");
            }
        } else {
            showError("Login failed. Please check your credentials.");
        }
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingSpinner.setVisibility(View.VISIBLE);
            loginButton.setText("");
            loginButton.setEnabled(false);
        } else {
            loadingSpinner.setVisibility(View.GONE);
            loginButton.setText(getString(R.string.login));
            loginButton.setEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }
}