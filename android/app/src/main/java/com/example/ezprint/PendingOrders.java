package com.example.ezprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezprint.adapters.PendingOrderAdapter;
import com.example.ezprint.models.Order;
import com.example.ezprint.models.OrderResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingOrders extends BaseActivity implements PendingOrderAdapter.OnOrderActionListener {

    private RecyclerView ordersRecyclerView;
    private ProgressBar loadingSpinner;
    private TextView messageText;
    private int shopId;
    private ImageView backArrow, homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the shop ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE);
        shopId = prefs.getInt("SHOP_ID", -1);

        if (shopId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        fetchPendingOrders();
    }

    private void initializeViews() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        ordersRecyclerView = findViewById(R.id.pending_orders_recycler_view);
        loadingSpinner = findViewById(R.id.loading_spinner);
        messageText = findViewById(R.id.message_text);

        backArrow = findViewById(R.id.back_arrow);
        homeIcon = findViewById(R.id.home_icon);

        backArrow.setOnClickListener(v -> onBackPressed());
        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(PendingOrders.this, Home.class));
            finish();
        });

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchPendingOrders() {
        showLoadingState();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getPendingOrders(shopId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body().getOrders();
                    if (orders.isEmpty()) {
                        showEmptyState("No pending orders found.");
                    } else {
                        showDataState(orders);
                    }
                } else {
                    showEmptyState("Failed to load orders.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                Log.e("PendingOrdersActivity", "API Failure: ", t);
                showEmptyState("Network Error. Please try again.");
            }
        });
    }

    private void showLoadingState() {
        loadingSpinner.setVisibility(View.VISIBLE);
        ordersRecyclerView.setVisibility(View.GONE);
        messageText.setVisibility(View.GONE);
    }

    private void showDataState(List<Order> orders) {
        loadingSpinner.setVisibility(View.GONE);
        messageText.setVisibility(View.GONE);
        // Pass 'this' as the listener to the adapter
        PendingOrderAdapter adapter = new PendingOrderAdapter(this, orders, this);
        ordersRecyclerView.setAdapter(adapter);
        ordersRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String message) {
        loadingSpinner.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.GONE);
        messageText.setText(message);
        messageText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOrderAction() {
        // After an action, check if the list is now empty
        if (ordersRecyclerView.getAdapter() != null && ordersRecyclerView.getAdapter().getItemCount() == 0) {
            showEmptyState("All pending orders have been handled!");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
