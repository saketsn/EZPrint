package com.example.ezprint;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ezprint.models.QrCodeResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.card.MaterialCardView;

import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrCode extends Fragment {

    private ImageView profileImage, qrCodeImage;
    private TextView shopNameText, ownerNameText, generatePrompt, linkText;
    private ProgressBar loadingSpinner;
    private ImageButton generateButton, shareButton, openLinkButton, saveButton;
    private MaterialCardView linkCard;
    private String currentQrCodeUrl = null;
    private String orderPageUrl = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        loadShopData();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        qrCodeImage = view.findViewById(R.id.qr_code_image);
        shopNameText = view.findViewById(R.id.shop_name_text);
        ownerNameText = view.findViewById(R.id.owner_name_text);
        generatePrompt = view.findViewById(R.id.generate_qr_prompt);
        loadingSpinner = view.findViewById(R.id.qr_loading_spinner);
        generateButton = view.findViewById(R.id.generate_button);
        shareButton = view.findViewById(R.id.share_button);
        linkCard = view.findViewById(R.id.link_card);
        linkText = view.findViewById(R.id.link_text);
        openLinkButton = view.findViewById(R.id.open_link_button);
        saveButton = view.findViewById(R.id.save_button);
    }

    private void loadShopData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        String shopName = prefs.getString("SHOP_NAME", "Shop Name");
        String ownerName = prefs.getString("OWNER_NAME", "Owner Name");
        String profileImageUrl = prefs.getString("PROFILE_PIC", null);
        currentQrCodeUrl = prefs.getString("QR_CODE_URL", null);

        shopNameText.setText(shopName);
        ownerNameText.setText(ownerName);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(RetrofitClient.BASE_URL + profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(profileImage);
        }

        if (currentQrCodeUrl != null && !currentQrCodeUrl.isEmpty()) {
            displayQrCode(currentQrCodeUrl);
        } else {
            showPrompt();
        }
    }

    private void setupClickListeners() {
        generateButton.setOnClickListener(v -> showConfirmationDialog());
        shareButton.setOnClickListener(v -> shareContent());
        saveButton.setOnClickListener(v -> saveQrCodeToGallery());

        // Copy link to clipboard when the card is clicked
        linkCard.setOnClickListener(v -> {
            if (orderPageUrl != null) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Shop Link", orderPageUrl);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });

        // Open link in browser
        openLinkButton.setOnClickListener(v -> {
            if (orderPageUrl != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderPageUrl));
                startActivity(browserIntent);
            }
        });
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Generate New QR Code?")
                .setMessage("This will invalidate your old QR code. Are you sure you want to proceed?")
                .setPositiveButton("Generate", (dialog, which) -> {
                    proceedWithGeneration();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void proceedWithGeneration() {
        showLoading();
        SharedPreferences prefs = requireActivity().getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);

        if (shopId == -1) {
            Toast.makeText(getContext(), "Error: Could not find shop ID.", Toast.LENGTH_SHORT).show();
            showPrompt();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.generateQrCode(shopId).enqueue(new Callback<QrCodeResponse>() {
            @Override
            public void onResponse(Call<QrCodeResponse> call, Response<QrCodeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentQrCodeUrl = response.body().getQrCodeUrl();
                    prefs.edit().putString("QR_CODE_URL", currentQrCodeUrl).apply();
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    displayQrCode(currentQrCodeUrl);
                } else {
                    Toast.makeText(getContext(), "Failed to generate QR Code.", Toast.LENGTH_SHORT).show();
                    showPrompt();
                }
            }

            @Override
            public void onFailure(Call<QrCodeResponse> call, Throwable t) {
                Log.e("QrCodeFragment", "API Failure: ", t);
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showPrompt();
            }
        });
    }

    private void displayQrCode(String qrCodeUrl) {
        loadingSpinner.setVisibility(View.GONE);
        generatePrompt.setVisibility(View.GONE);
        qrCodeImage.setVisibility(View.VISIBLE);
        shareButton.setEnabled(true);
        linkCard.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);

        String fullQrUrl = RetrofitClient.BASE_URL + qrCodeUrl;
        SharedPreferences prefs = requireActivity().getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);
        orderPageUrl = RetrofitClient.BASE_URL + "order/" + shopId;

        linkText.setText(orderPageUrl);
        Glide.with(this).load(fullQrUrl).into(qrCodeImage);
    }

    private void showPrompt() {
        loadingSpinner.setVisibility(View.GONE);
        generatePrompt.setVisibility(View.VISIBLE);
        qrCodeImage.setVisibility(View.GONE);
        shareButton.setEnabled(false);
        linkCard.setVisibility(View.GONE);
        saveButton.setEnabled(false);
    }

    private void showLoading() {
        loadingSpinner.setVisibility(View.VISIBLE);
        generatePrompt.setVisibility(View.GONE);
        qrCodeImage.setVisibility(View.GONE);
        shareButton.setEnabled(false);
        linkCard.setVisibility(View.GONE);
        saveButton.setEnabled(false);
    }

    private void shareContent() {
        if (orderPageUrl != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = "Place your print order at my shop using this link:\n" + orderPageUrl;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share your shop link"));
        } else {
            Toast.makeText(getContext(), "Please generate a QR code first.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQrCodeToGallery() {
        // Get the bitmap from the ImageView
        BitmapDrawable drawable = (BitmapDrawable) qrCodeImage.getDrawable();
        if (drawable == null) {
            Toast.makeText(getContext(), "QR Code image is not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = drawable.getBitmap();

        // Create a filename
        String filename = "EZPrint_QR_" + System.currentTimeMillis() + ".png";

        // Use MediaStore to save the image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        // Save to the "Pictures" directory
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EZPrint");
        }

        Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(getContext(), "QR Code saved to Gallery!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("QrCodeFragment", "Error saving QR code", e);
            Toast.makeText(getContext(), "Failed to save QR code.", Toast.LENGTH_SHORT).show();
        }
    }

}

