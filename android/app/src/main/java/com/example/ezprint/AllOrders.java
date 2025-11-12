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
import com.example.ezprint.adapters.OrderHistoryAdapter;
import com.example.ezprint.models.Order;
import com.example.ezprint.models.OrderResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllOrders extends AppCompatActivity {

    private TabLayout filterTabs;
    private RecyclerView ordersRecyclerView;
    private ProgressBar loadingSpinner;
    private TextView messageText;
    private OrderHistoryAdapter adapter;
    private int shopId;
    private ImageView backArrow, homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_orders);

        // --- FIX FOR NOTCH OVERLAP ---
        // This listener gets the size of the system bars (status bar, navigation bar)
        // and applies them as padding to your main layout, preventing overlap.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("EZPrint_Prefs", MODE_PRIVATE);
        shopId = prefs.getInt("SHOP_ID", -1);

        if (shopId == -1) {
            // Handle error, maybe finish activity and show a toast
            Toast.makeText(this, "Session Error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupTabs();

        // Initial data load for the "Daily" tab
        fetchOrders("daily");
    }

    private void initializeViews() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        filterTabs = findViewById(R.id.filter_tabs);
        ordersRecyclerView = findViewById(R.id.orders_history_recycler_view);
        loadingSpinner = findViewById(R.id.loading_spinner);
        messageText = findViewById(R.id.message_text);

        backArrow = findViewById(R.id.back_arrow);
        homeIcon = findViewById(R.id.home_icon);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(this, new ArrayList<>());
        ordersRecyclerView.setAdapter(adapter);

        backArrow.setOnClickListener(v -> onBackPressed());
        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(AllOrders.this, Home.class));
            finish();
        });
    }

    private void setupTabs() {
        filterTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String filter = "daily";
                switch (tab.getPosition()) {
                    case 1: filter = "weekly"; break;
                    case 2: filter = "monthly"; break;
                }
                fetchOrders(filter);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchOrders(String filter) {
        showLoadingState();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getAllOrders(shopId, filter).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body().getOrders();
                    if (orders.isEmpty()) {
                        showEmptyState("No orders found for this period.");
                    } else {
                        showDataState(orders);
                    }
                } else {
                    showEmptyState("Failed to load orders.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                Log.e("AllOrdersActivity", "API Failure: ", t);
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
        adapter = new OrderHistoryAdapter(this, orders);
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
