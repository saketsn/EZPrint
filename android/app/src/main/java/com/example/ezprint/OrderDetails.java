package com.example.ezprint;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.ezprint.adapters.DocumentAdapter;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.Order;
import com.example.ezprint.models.OrderDetailsResponse;
import com.example.ezprint.models.OrderStatusUpdateRequest;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetails extends BaseActivity {

    private int orderId;
    private TextView orderCodeText, orderDateText, orderStatusText, studentNameText, studentContactText, studentEmailText, idProofLabel, notesText, pendingActionPrompt;
    private ImageView idProofImage, homeIcon;
    private RecyclerView documentsRecyclerView;
    private EditText finalAmountEditText;
    private Button completeOrderButton, cancelOrderButton;
    private ProgressBar mainSpinner, buttonSpinner;
    private LinearLayout contentLayout;
    private View idProofLayout;
    private LottieAnimationView successAnimation, cancelAnimation;
    private View animationOverlay;
    private TextInputLayout finalAmountLayout;
    private LinearLayout actionButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Order ID from the intent
        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Error: Invalid Order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        fetchOrderDetails();
    }

    private void initializeViews() {
        // Main layout containers
        mainSpinner = findViewById(R.id.main_loading_spinner);
        contentLayout = findViewById(R.id.content_layout);

        // Order Info Card
        orderCodeText = findViewById(R.id.order_code_text);
        orderDateText = findViewById(R.id.order_date_text);
        orderStatusText = findViewById(R.id.order_status_text);
        notesText = findViewById(R.id.notes_text);
        homeIcon = findViewById(R.id.more_options_icon);

        // Student Info Card
        studentNameText = findViewById(R.id.student_name_text);
        studentContactText = findViewById(R.id.student_phone_text);
        studentEmailText = findViewById(R.id.student_email_text);
        idProofLabel = findViewById(R.id.id_proof_label);
        idProofLayout = findViewById(R.id.id_proof_layout);

        // Documents
        documentsRecyclerView = findViewById(R.id.documents_recycler_view);

        // Payment
        finalAmountEditText = findViewById(R.id.final_amount_edit_text);
        completeOrderButton = findViewById(R.id.complete_order_button);
        cancelOrderButton = findViewById(R.id.cancel_order_button);
        buttonSpinner = findViewById(R.id.button_progress_bar);
        pendingActionPrompt = findViewById(R.id.pending_action_prompt);

        finalAmountLayout = findViewById(R.id.final_amount_layout);
        actionButtonsLayout = findViewById(R.id.action_buttons_layout);

        animationOverlay = findViewById(R.id.animation_overlay);
        successAnimation = findViewById(R.id.success_animation_view);
        cancelAnimation = findViewById(R.id.cancel_animation_view);
    }

    private void setupClickListeners() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        completeOrderButton.setOnClickListener(v -> showConfirmationDialog("Complete"));
        cancelOrderButton.setOnClickListener(v -> showConfirmationDialog("Cancel"));

        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(OrderDetails.this, Home.class));
            finish();
        });
    }

    private void fetchOrderDetails() {
        mainSpinner.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getOrderDetails(orderId).enqueue(new Callback<OrderDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailsResponse> call, @NonNull Response<OrderDetailsResponse> response) {
                mainSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    contentLayout.setVisibility(View.VISIBLE);
                    populateUI(response.body().getOrder());
                } else {
                    Toast.makeText(OrderDetails.this, "Failed to load order details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailsResponse> call, @NonNull Throwable t) {
                mainSpinner.setVisibility(View.GONE);
                Log.e("OrderDetails", "API Failure: ", t);
                Toast.makeText(OrderDetails.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(Order order) {
        // ... (populate order info, student info, documents list, etc.)
        orderCodeText.setText(order.getOrderUid());
        notesText.setText(order.getNotes() != null && !order.getNotes().isEmpty() ? order.getNotes() : "No additional notes provided.");
        setStatusStyle(order.getStatus());

        try {
            SimpleDateFormat incomingFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outgoingFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            Date date = incomingFormat.parse(order.getCreatedAt());
            orderDateText.setText(outgoingFormat.format(date));
        } catch (ParseException e) {
            orderDateText.setText(order.getCreatedAt());
        }

        studentNameText.setText(order.getCustomerName());
        studentContactText.setText(order.getStudentPhone());
        studentEmailText.setText(order.getStudentEmail());

        String status = order.getStatus();
        if (status != null && status.length() > 0) {
            String status2 = status.substring(0, 1).toUpperCase() + status.substring(1);
            orderStatusText.setText(status2);
        }

        Order.Document idProof = order.getDocuments().stream()
                .filter(doc -> "id_proof".equals(doc.getDocType()))
                .findFirst().orElse(null);

        if (idProof != null) {
            idProofLayout.setVisibility(View.VISIBLE);
            TextView proofFileName = idProofLayout.findViewById(R.id.proof_file_name);
            String[] urlParts = idProof.getFileUrl().split("/");
            proofFileName.setText(urlParts[urlParts.length - 1]);
            idProofLayout.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(RetrofitClient.BASE_URL + idProof.getFileUrl()));
                startActivity(browserIntent);
            });
        } else {
            idProofLabel.setVisibility(View.GONE);
            idProofLayout.setVisibility(View.GONE);
        }

        List<Order.Document> printFiles = order.getDocuments().stream()
                .filter(doc -> "print_file".equals(doc.getDocType()))
                .collect(Collectors.toList());

        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DocumentAdapter documentAdapter = new DocumentAdapter(this, printFiles);
        documentsRecyclerView.setAdapter(documentAdapter);

        // --- UPDATED: Logic to handle different order statuses ---
        if ("pending".equalsIgnoreCase(status)) {
            finalAmountLayout.setVisibility(View.GONE);
            actionButtonsLayout.setVisibility(View.GONE);
            pendingActionPrompt.setVisibility(View.VISIBLE);
        } else if ("completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
            if (order.getAmount() != null) {
                finalAmountEditText.setText(String.format(Locale.getDefault(), "%.2f", order.getAmount()));
            }
            finalAmountEditText.setEnabled(false);
            completeOrderButton.setEnabled(false);
            cancelOrderButton.setEnabled(false);

            if ("completed".equalsIgnoreCase(status)) {
                completeOrderButton.setText("Order Completed");
                cancelOrderButton.setVisibility(View.GONE);
            } else {
                completeOrderButton.setVisibility(View.GONE);
                cancelOrderButton.setText("Order " + status);
            }
            finalAmountLayout.setVisibility(View.VISIBLE);
            actionButtonsLayout.setVisibility(View.VISIBLE);
            pendingActionPrompt.setVisibility(View.GONE);
        } else {
            if (order.getAmount() != null) {
                finalAmountEditText.setText(String.format(Locale.getDefault(), "%.2f", order.getAmount()));
            }
            finalAmountLayout.setVisibility(View.VISIBLE);
            actionButtonsLayout.setVisibility(View.VISIBLE);
            pendingActionPrompt.setVisibility(View.GONE);
        }
    }

    private void setStatusStyle(String status) {
        if (status == null) return;

        int backgroundRes;
        int textColorRes;
        String statusText = status.toLowerCase();

        switch (statusText) {
            case "pending":
                backgroundRes = R.drawable.status_background_pending;
                textColorRes = R.color.white;
                break;
            case "printing":
            case "accepted":
            case "in progress":
                backgroundRes = R.drawable.status_background_progress;
                textColorRes = R.color.white;
                break;
            case "cancelled":
            case "rejected":
                backgroundRes = R.drawable.status_background_cancelled;
                textColorRes = R.color.white;
                break;
            case "completed":
            default:
                backgroundRes = R.drawable.status_background_completed;
                textColorRes = R.color.white;
                break;
        }
        orderStatusText.setBackgroundResource(backgroundRes);
        orderStatusText.setTextColor(ContextCompat.getColor(this, textColorRes));
    }

    private void showConfirmationDialog(String action) {
        new AlertDialog.Builder(this)
                .setTitle(action + " Order?")
                .setMessage("Are you sure you want to " + action.toLowerCase() + " this order?")
                .setPositiveButton("Yes, " + action, (dialog, which) -> {
                    if ("Complete".equals(action)) {
                        updateOrderStatus("completed");
                    } else {
                        updateOrderStatus("cancelled");
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateOrderStatus(String status) {
        String amountStr = finalAmountEditText.getText().toString().trim();
        Double finalAmount = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);

        if ("completed".equals(status) && finalAmount <= 0) {
            finalAmountEditText.setError("Amount must be greater than 0 for completed orders.");
            return;
        }

        showButtonLoading(true);
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(status, finalAmount);
        ApiService apiService = RetrofitClient.getApiService();
        apiService.updateOrderStatus(orderId, request).enqueue(new Callback<ApiErrorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                showButtonLoading(false);
                if (response.isSuccessful()) {
                    playAnimation("completed".equals(status));
                } else {
                    Toast.makeText(OrderDetails.this, "Failed to update order.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                showButtonLoading(false);
                Toast.makeText(OrderDetails.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playAnimation(boolean isSuccess) {
        animationOverlay.setVisibility(View.VISIBLE);
        LottieAnimationView animationView = isSuccess ? successAnimation : cancelAnimation;

        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();

        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Use a handler to delay the navigation
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    setResult(RESULT_OK); // Signal to previous screen to refresh
                    finish();
                }, 1000); // A 1-second delay after the animation finishes
            }

            // Other listener methods...
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void showButtonLoading(boolean isLoading) {
        buttonSpinner.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        completeOrderButton.setEnabled(!isLoading);
        cancelOrderButton.setEnabled(!isLoading);

        if(isLoading){
            completeOrderButton.setText("");
        } else {
            completeOrderButton.setText("Complete");
        }
    }
}
