package com.example.ezprint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ezprint.adapters.OrderAdapter;
import com.example.ezprint.models.DashboardStats;
import com.example.ezprint.models.DashboardStatsResponse;
import com.example.ezprint.models.OrderResponse;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private RecyclerView ordersRecyclerView;
    private TextView noOrdersTextView;
    private ProgressBar loadingProgressBar;
    private TextView totalOrdersHeader, pendingValue, inProgressValue, completedValue,
            revenueValue, monthlyOrdersValue, newCustomersValue, revenueChangeText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        // Initialize Views
        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        noOrdersTextView = view.findViewById(R.id.no_orders_text);
        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        TextView dateTextView = view.findViewById(R.id.date_text);

        // Setup RecyclerView with a divider
        // --- UPDATED: Setup RecyclerView with a divider ---
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider));
        ordersRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final Drawable divider = ContextCompat.getDrawable(requireContext(), R.drawable.divider);

            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int left = parent.getPaddingLeft();
                int right = parent.getWidth() - parent.getPaddingRight();

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + divider.getIntrinsicHeight();

                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
        });


        // Setup "Show all" click listener
        TextView showAllOrders = view.findViewById(R.id.show_all_orders);
        showAllOrders.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllOrders.class);
            startActivity(intent);
        });

        // Set the current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        dateTextView.setText(sdf.format(new Date()));

        // Fetch the recent orders
        fetchRecentOrders();
        fetchDashboardStats();
    }

    private void initializeViews(View view) {
        // ... (initialize existing views)
        totalOrdersHeader = view.findViewById(R.id.balance_value);
        pendingValue = view.findViewById(R.id.pending_orders_value);
        inProgressValue = view.findViewById(R.id.in_progress_orders_value);
        completedValue = view.findViewById(R.id.completed_orders_value);
        revenueValue = view.findViewById(R.id.todays_revenue_value);
        monthlyOrdersValue = view.findViewById(R.id.monthly_orders_value);
        newCustomersValue = view.findViewById(R.id.new_customers_value);
        revenueChangeText = view.findViewById(R.id.revenue_change_text);
    }

    private void fetchRecentOrders() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        noOrdersTextView.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.GONE);

        SharedPreferences prefs = getActivity().getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);

        if (shopId == -1) {
            loadingProgressBar.setVisibility(View.GONE);
            noOrdersTextView.setText("Could not find user session.");
            noOrdersTextView.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getRecentOrders(shopId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                loadingProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.ezprint.models.Order> orders = response.body().getOrders();
                    if (orders.isEmpty()) {
                        noOrdersTextView.setText("No recent orders found.");
                        noOrdersTextView.setVisibility(View.VISIBLE);
                        ordersRecyclerView.setVisibility(View.GONE);
                    } else {
                        noOrdersTextView.setVisibility(View.GONE);
                        ordersRecyclerView.setVisibility(View.VISIBLE);
                        OrderAdapter orderAdapter = new OrderAdapter(getContext(), orders);
                        ordersRecyclerView.setAdapter(orderAdapter);
                    }
                } else {
                    noOrdersTextView.setText("Failed to load orders.");
                    noOrdersTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                noOrdersTextView.setText("Network Error. Please try again.");
                noOrdersTextView.setVisibility(View.VISIBLE);
                Log.e("DashboardFragment", "API call failed: ", t);
            }
        });
    }

    private void fetchDashboardStats() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);
        if (shopId == -1) return;

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getDashboardStats(shopId).enqueue(new Callback<DashboardStatsResponse>() {
            @Override
            public void onResponse(Call<DashboardStatsResponse> call, Response<DashboardStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateStatsUI(response.body().getStats());
                } else {
                    // Handle error silently or show a small message
                }
            }
            @Override
            public void onFailure(Call<DashboardStatsResponse> call, Throwable t) {
                Log.e("DashboardStats", "API call failed: ", t);
            }
        });
    }

    private void populateStatsUI(DashboardStats stats) {
        totalOrdersHeader.setText(String.valueOf(stats.getTodaysTotalOrders()));
        pendingValue.setText(String.valueOf(stats.getPendingOrders()));
        inProgressValue.setText(String.valueOf(stats.getInProgressOrders()));
        completedValue.setText(String.valueOf(stats.getCompletedOrders()));
        revenueValue.setText(String.format(Locale.getDefault(), "₹%,.2f", stats.getTodaysRevenue()));
        monthlyOrdersValue.setText(String.valueOf(stats.getMonthlyOrders()));
        newCustomersValue.setText(String.valueOf(stats.getNewCustomers()));

        // Handle the percentage change text and color
        double change = stats.getRevenueChangePercentage();
        if (change > 0) {
            revenueChangeText.setText(String.format(Locale.getDefault(), "▲ %.1f%% from previous month", change));
            revenueChangeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_green));
        } else if (change < 0) {
            revenueChangeText.setText(String.format(Locale.getDefault(), "▼ %.1f%% from previous month", Math.abs(change)));
            revenueChangeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            revenueChangeText.setText("No change from previous month");
            revenueChangeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }
    }
}

