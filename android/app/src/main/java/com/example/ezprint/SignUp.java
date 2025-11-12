package com.example.ezprint;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.RegisterRequest;
import com.example.ezprint.models.RegisterResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private TextInputEditText shopNameEditText, ownerNameEditText, emailEditText, phoneEditText, passwordEditText, addressEditText;
    private Button signUpButton;
    private TextView loginLink, errorTextView;
    private ProgressBar loadingSpinner;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // This handles edge-to-edge display insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });

        // Initialize views
        shopNameEditText = findViewById(R.id.shop_name_edit_text);
        ownerNameEditText = findViewById(R.id.owner_name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        addressEditText = findViewById(R.id.address_edit_text);
        signUpButton = findViewById(R.id.signup_button);
        loginLink = findViewById(R.id.login_link);
        errorTextView = findViewById(R.id.error_message_text_view);
        loadingSpinner = findViewById(R.id.loading_spinner);

        // Set click listener for the sign-up button
        signUpButton.setOnClickListener(v -> performSignUp());

        // Set click listener for the login link
        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Add TextWatcher to clear errors when the user types
        TextWatcher errorClearer = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        shopNameEditText.addTextChangedListener(errorClearer);
        ownerNameEditText.addTextChangedListener(errorClearer);
        emailEditText.addTextChangedListener(errorClearer);
        phoneEditText.addTextChangedListener(errorClearer);
        passwordEditText.addTextChangedListener(errorClearer);
    }

    private void performSignUp() {
        // Get text from all fields
        String shopName = shopNameEditText.getText().toString().trim();
        String ownerName = ownerNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Perform validation
        if (!isFormValid(shopName, ownerName, email, phone, password)) {
            return;
        }

        // Show loading state
        showLoading(true);

        // Create request object
        RegisterRequest registerRequest = new RegisterRequest(shopName, ownerName, email, phone, password, address);
        ApiService apiService = RetrofitClient.getApiService();

        // Make API call
        long requestStartTime = System.currentTimeMillis();
        apiService.registerShop(registerRequest).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                long elapsedTime = System.currentTimeMillis() - requestStartTime;
                long delay = Math.max(0, 1000 - elapsedTime);

                handler.postDelayed(() -> {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        // Handle successful registration
                        RegisterResponse registerResponse = response.body();
                        Toast.makeText(SignUp.this, registerResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Redirect to the login screen
                        Intent intent = new Intent(SignUp.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle API errors
                        handleApiError(response);
                    }
                }, delay);
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                long elapsedTime = System.currentTimeMillis() - requestStartTime;
                long delay = Math.max(0, 1000 - elapsedTime);

                handler.postDelayed(() -> {
                    showLoading(false);
                    Log.e("SignUpFailure", "Network error: ", t);
                    showError("Network error. Please check your connection.");
                }, delay);
            }
        });
    }

    private boolean isFormValid(String shopName, String ownerName, String email, String phone, String password) {
        // Your existing validation logic from Login, adapted for the new fields
        boolean isValid = true;

        if (shopName.isEmpty()) {
            shopNameEditText.setError("Shop name is required");
            isValid = false;
        }

        if (ownerName.isEmpty()) {
            ownerNameEditText.setError("Owner name is required");
            isValid = false;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            isValid = false;
        }

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
                Gson gson = new Gson();
                ApiErrorResponse errorResponse = gson.fromJson(errorBodyString, ApiErrorResponse.class);

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
            showError("Sign up failed. Please try again.");
        }
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingSpinner.setVisibility(View.VISIBLE);
            signUpButton.setText("");
            signUpButton.setEnabled(false);
        } else {
            loadingSpinner.setVisibility(View.GONE);
            signUpButton.setText("Sign Up");
            signUpButton.setEnabled(true);
        }
    }

    private void clearErrors() {
        shopNameEditText.setError(null);
        ownerNameEditText.setError(null);
        emailEditText.setError(null);
        phoneEditText.setError(null);
        passwordEditText.setError(null);
        errorTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }
}
