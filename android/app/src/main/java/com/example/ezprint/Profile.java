package com.example.ezprint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.ImageUploadResponse;
import com.example.ezprint.models.ProfileUpdateRequest;
import com.example.ezprint.models.ProfileUpdateResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends BaseActivity {

    private boolean isEditMode = false;
    private boolean profileDataChanged = false;

    // UI Components
    private ImageView backArrow, homeIcon, profileImage, cameraIcon, closeEditButton;
    private TextView headerTitle, ownerNameText, shopNameText, emailText, phoneText, addressText, editErrorText;
    private LinearLayout displayInfoLayout, editInfoLayout;
    private MaterialCardView publicInfoCard, privateInfoCard;
    private TextInputEditText editOwnerName, editShopName, editEmail, editPhone, editAddress;
    private Button saveButton;
    private ProgressBar saveProgressBar, imageUploadProgressBar;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::uploadImageToServer
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        loadProfileData();
        setupClickListeners();
    }

    private void initializeViews() {
        headerTitle = findViewById(R.id.header_title);
        backArrow = findViewById(R.id.back_arrow);
        homeIcon = findViewById(R.id.home_icon);
        profileImage = findViewById(R.id.profile_image);
        cameraIcon = findViewById(R.id.camera_icon);
        displayInfoLayout = findViewById(R.id.display_info_layout);
        ownerNameText = findViewById(R.id.owner_name_text);
        shopNameText = findViewById(R.id.shop_name_text);
        emailText = findViewById(R.id.email_text);
        phoneText = findViewById(R.id.phone_text);
        addressText = findViewById(R.id.address_text);
        publicInfoCard = findViewById(R.id.public_info_card);
        privateInfoCard = findViewById(R.id.private_info_card);
        editInfoLayout = findViewById(R.id.edit_info_layout);
        editOwnerName = findViewById(R.id.edit_owner_name);
        editShopName = findViewById(R.id.edit_shop_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editAddress = findViewById(R.id.edit_address);
        saveButton = findViewById(R.id.save_button);
        saveProgressBar = findViewById(R.id.save_progress_bar);
        closeEditButton = findViewById(R.id.close_edit_button);
        imageUploadProgressBar = findViewById(R.id.image_upload_progress_bar);
        editErrorText = findViewById(R.id.edit_error_message_text_view);
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> onBackPressed());
        closeEditButton.setOnClickListener(v -> toggleEditMode(false));
        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, Home.class));
            finish();
        });
        cameraIcon.setOnClickListener(v -> mGetContent.launch("image/*"));
        View.OnClickListener editClickListener = v -> toggleEditMode(true);
        publicInfoCard.setOnClickListener(editClickListener);
        privateInfoCard.setOnClickListener(editClickListener);
        saveButton.setOnClickListener(v -> saveProfileData());

        TextWatcher errorClearer = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editErrorText.setVisibility(View.GONE);
                editOwnerName.setError(null);
                editShopName.setError(null);
                editEmail.setError(null);
                editPhone.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        editOwnerName.addTextChangedListener(errorClearer);
        editShopName.addTextChangedListener(errorClearer);
        editEmail.addTextChangedListener(errorClearer);
        editPhone.addTextChangedListener(errorClearer);
        editAddress.addTextChangedListener(errorClearer);
    }

    private void toggleEditMode(boolean enableEdit) {
        isEditMode = enableEdit;
        if (enableEdit) {
            displayInfoLayout.setVisibility(View.GONE);
            editInfoLayout.setVisibility(View.VISIBLE);
            headerTitle.setText("Edit Profile");
            loadProfileData();
        } else {
            displayInfoLayout.setVisibility(View.VISIBLE);
            editInfoLayout.setVisibility(View.GONE);
            headerTitle.setText("Profile");
        }
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        String ownerName = prefs.getString("OWNER_NAME", "");
        String shopName = prefs.getString("SHOP_NAME", "");
        String email = prefs.getString("EMAIL", "");
        String phone = prefs.getString("PHONE", "");
        String address = prefs.getString("ADDRESS", "");
        String profileImageUrl = prefs.getString("PROFILE_PIC", null);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            String fullUrl = RetrofitClient.BASE_URL + profileImageUrl;
            Glide.with(this).load(fullUrl).circleCrop().placeholder(R.drawable.ic_avatar_placeholder).into(profileImage);
        }

        ownerNameText.setText(ownerName);
        shopNameText.setText(shopName);
        emailText.setText(email);
        phoneText.setText(phone);
        addressText.setText(address);

        editOwnerName.setText(ownerName);
        editShopName.setText(shopName);
        editEmail.setText(email);
        editPhone.setText(phone);
        editAddress.setText(address);
    }

    private void saveProfileData() {
        String newOwnerName = editOwnerName.getText().toString().trim();
        String newShopName = editShopName.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String newAddress = editAddress.getText().toString().trim();

        if (!isEditFormValid(newOwnerName, newShopName, newEmail, newPhone)) {
            return;
        }

        showLoading(true);
        editErrorText.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);
        if (shopId == -1) {
            showError("Error: User session not found.");
            showLoading(false);
            return;
        }

        ProfileUpdateRequest request = new ProfileUpdateRequest(newShopName, newOwnerName, newEmail, newPhone, newAddress);
        ApiService apiService = RetrofitClient.getApiService();

        apiService.updateProfile(shopId, request).enqueue(new Callback<ProfileUpdateResponse>() {
            @Override
            public void onResponse(Call<ProfileUpdateResponse> call, Response<ProfileUpdateResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("OWNER_NAME", newOwnerName);
                    editor.putString("SHOP_NAME", newShopName);
                    editor.putString("EMAIL", newEmail);
                    editor.putString("PHONE", newPhone);
                    editor.putString("ADDRESS", newAddress);
                    editor.apply();
                    profileDataChanged = true;
                    loadProfileData();
                    toggleEditMode(false);
                    Toast.makeText(Profile.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBodyString = response.errorBody().string();
                        ApiErrorResponse errorResponse = new Gson().fromJson(errorBodyString, ApiErrorResponse.class);
                        showError(errorResponse.getError());
                    } catch (Exception e) {
                        showError("Failed to update profile.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileUpdateResponse> call, Throwable t) {
                showLoading(false);
                Log.e("ProfileUpdate", "API call failed: ", t);
                showError("Network error. Please try again.");
            }
        });
    }

    private void showError(String message) {
        editErrorText.setText(message);
        editErrorText.setVisibility(View.VISIBLE);
    }

    private boolean isEditFormValid(String ownerName, String shopName, String email, String phone) {
        if (ownerName.isEmpty()) {
            editOwnerName.setError("Owner name is required");
            return false;
        }
        if (shopName.isEmpty()) {
            editShopName.setError("Shop name is required");
            return false;
        }
        if (phone.isEmpty() || !phone.matches("\\d{10}")) {
            editPhone.setError("A valid 10-digit phone number is required");
            return false;
        }
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            saveProgressBar.setVisibility(View.VISIBLE);
            saveButton.setText("");
            saveButton.setEnabled(false);
        } else {
            saveProgressBar.setVisibility(View.GONE);
            saveButton.setText("Save Changes");
            saveButton.setEnabled(true);
        }
    }

    private void uploadImageToServer(Uri imageUri) {
        if (imageUri == null) return;
        imageUploadProgressBar.setVisibility(View.VISIBLE);

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(imageUri)), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("profile_pic", "profile.jpg", requestFile);
            SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
            int shopId = prefs.getInt("SHOP_ID", -1);

            if (shopId == -1) {
                Toast.makeText(this, "Error: Could not find shop ID.", Toast.LENGTH_SHORT).show();
                imageUploadProgressBar.setVisibility(View.GONE);
                return;
            }

            ApiService apiService = RetrofitClient.getApiService();
            apiService.uploadProfilePicture(shopId, body).enqueue(new Callback<ImageUploadResponse>() {
                @Override
                public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                    imageUploadProgressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        String newImageUrl = response.body().getImageUrl();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("PROFILE_PIC", newImageUrl);
                        editor.apply();
                        profileDataChanged = true;
                        Toast.makeText(Profile.this, "Image updated!", Toast.LENGTH_SHORT).show();
                        loadProfileData();
                    } else {
                        Toast.makeText(Profile.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                    imageUploadProgressBar.setVisibility(View.GONE);
                    Log.e("ImageUpload", "Failure: ", t);
                    Toast.makeText(Profile.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            imageUploadProgressBar.setVisibility(View.GONE);
            Log.e("ImageUpload", "Error preparing image for upload", e);
            Toast.makeText(this, "Error: Could not read image file.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        if (profileDataChanged) {
            setResult(RESULT_OK);
        }
        super.finish();
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

