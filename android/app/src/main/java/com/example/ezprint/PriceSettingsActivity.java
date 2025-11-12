package com.example.ezprint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.PriceSettings;
import com.example.ezprint.models.PriceSettingsResponse;
import com.example.ezprint.models.PriceUpdateResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceSettingsActivity extends BaseActivity {

    private boolean isEditMode = false;

    // UI Components
    private LinearLayout displayPricesLayout, editPricesLayout;
    private MaterialCardView priceDisplayCard;
    private TextView displayPriceBw, displayPriceColor, displayPriceBwDuplex, displayPriceColorDuplex, errorText;
    private EditText editPriceBw, editPriceColor, editPriceBwDuplex, editPriceColorDuplex;
    private Button saveButton;
    private ProgressBar saveProgressBar;
    private ImageView closeEditButton, homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
        fetchCurrentPrices();
    }

    private void initializeViews() {
        // Display Views
        displayPricesLayout = findViewById(R.id.display_prices_layout);
        priceDisplayCard = findViewById(R.id.price_display_card);
        displayPriceBw = findViewById(R.id.display_price_bw);
        displayPriceColor = findViewById(R.id.display_price_color);
        displayPriceBwDuplex = findViewById(R.id.display_price_bw_duplex);
        displayPriceColorDuplex = findViewById(R.id.display_price_color_duplex);

        // Edit Views
        editPricesLayout = findViewById(R.id.edit_prices_layout);
        editPriceBw = findViewById(R.id.edit_price_bw);
        editPriceColor = findViewById(R.id.edit_price_color);
        editPriceBwDuplex = findViewById(R.id.edit_price_bw_duplex);
        editPriceColorDuplex = findViewById(R.id.edit_price_color_duplex);
        saveButton = findViewById(R.id.save_button);
        saveProgressBar = findViewById(R.id.save_progress_bar);
        errorText = findViewById(R.id.error_message_text_view);

        homeIcon = findViewById(R.id.home_icon);
        closeEditButton = findViewById(R.id.close_edit_button);
    }

    private void setupClickListeners() {
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        priceDisplayCard.setOnClickListener(v -> toggleEditMode(true));
        saveButton.setOnClickListener(v -> savePriceData());

        closeEditButton.setOnClickListener(v -> toggleEditMode(false));
        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(PriceSettingsActivity.this, Home.class));
            finish();
        });

        // TextWatcher to clear error message when user types
        TextWatcher errorClearer = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorText.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        editPriceBw.addTextChangedListener(errorClearer);
        editPriceColor.addTextChangedListener(errorClearer);
        editPriceBwDuplex.addTextChangedListener(errorClearer);
        editPriceColorDuplex.addTextChangedListener(errorClearer);
    }

    private void toggleEditMode(boolean enableEdit) {
        isEditMode = enableEdit;
        displayPricesLayout.setVisibility(enableEdit ? View.GONE : View.VISIBLE);
        editPricesLayout.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
    }

    private void fetchCurrentPrices() {
        // TODO: Show a proper loading state for the whole view
        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);
        if (shopId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getPriceSettings(shopId).enqueue(new Callback<PriceSettingsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PriceSettingsResponse> call, @NonNull Response<PriceSettingsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populatePriceFields(response.body().getPrices());
                } else {
                    Toast.makeText(PriceSettingsActivity.this, "Failed to load prices.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PriceSettingsResponse> call, @NonNull Throwable t) {
                Toast.makeText(PriceSettingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populatePriceFields(PriceSettings prices) {
        String rupee = "₹";
        displayPriceBw.setText(rupee + prices.getPricePerBwPage());
        displayPriceColor.setText(rupee + prices.getPricePerColorPage());
        displayPriceBwDuplex.setText(rupee + prices.getPricePerBwDuplex());
        displayPriceColorDuplex.setText(rupee + prices.getPricePerColorDuplex());

        editPriceBw.setText(String.valueOf(prices.getPricePerBwPage()));
        editPriceColor.setText(String.valueOf(prices.getPricePerColorPage()));
        editPriceBwDuplex.setText(String.valueOf(prices.getPricePerBwDuplex()));
        editPriceColorDuplex.setText(String.valueOf(prices.getPricePerColorDuplex()));
    }

    private void savePriceData() {
        if (!isFormValid()) return;

        double bwPrice = Double.parseDouble(editPriceBw.getText().toString());
        double colorPrice = Double.parseDouble(editPriceColor.getText().toString());
        double bwDuplexPrice = Double.parseDouble(editPriceBwDuplex.getText().toString());
        double colorDuplexPrice = Double.parseDouble(editPriceColorDuplex.getText().toString());

        showLoading(true);

        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);

        PriceSettings newPrices = new PriceSettings(bwPrice, colorPrice, bwDuplexPrice, colorDuplexPrice);
        ApiService apiService = RetrofitClient.getApiService();
        apiService.updatePriceSettings(shopId, newPrices).enqueue(new Callback<PriceUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<PriceUpdateResponse> call, @NonNull Response<PriceUpdateResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Use the success message for the Toast
                    Toast.makeText(PriceSettingsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    fetchCurrentPrices();
                    toggleEditMode(false);
                } else {
                    // Parse the error body using the ApiErrorResponse class
                    try {
                        String errorBody = response.errorBody().string();
                        ApiErrorResponse error = new Gson().fromJson(errorBody, ApiErrorResponse.class);
                        errorText.setText(error.getError());
                        errorText.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        errorText.setText("Failed to update prices.");
                        errorText.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PriceUpdateResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("PriceSettings", "API Failure: ", t);
                errorText.setText("Network error. Please try again.");
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isFormValid() {
        // You can add more detailed validation here (e.g., check for valid numbers)
        return !editPriceBw.getText().toString().isEmpty() &&
                !editPriceColor.getText().toString().isEmpty() &&
                !editPriceBwDuplex.getText().toString().isEmpty() &&
                !editPriceColorDuplex.getText().toString().isEmpty();
    }

    private void showLoading(boolean isLoading) {
        saveProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveButton.setText(isLoading ? "" : "Save Changes");
        saveButton.setEnabled(!isLoading);
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            toggleEditMode(false);
        } else {
            super.onBackPressed();
        }
    }
}

