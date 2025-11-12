package com.example.ezprint;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezprint.adapters.OrderGridAdapter;
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

public class SearchOrder extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar loadingSpinner;
    private TextView messageText;
    private TabLayout filterTabs;
    private OrderGridAdapter orderGridAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentSearchQuery = "";
    private String currentStatusFilter = "completed"; // Default to the first tab

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        setupSearch();
        setupTabs();
        performSearch(currentSearchQuery, currentStatusFilter); // Initial load
    }

    private void initializeViews(View view) {
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        loadingSpinner = view.findViewById(R.id.search_loading_spinner);
        messageText = view.findViewById(R.id.search_message_text);
        filterTabs = view.findViewById(R.id.filter_tabs);
    }

    private void setupRecyclerView() {
        // CORRECTED: Use a GridLayoutManager with 2 columns
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        orderGridAdapter = new OrderGridAdapter(getContext(), new ArrayList<>());
        searchResultsRecyclerView.setAdapter(orderGridAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
            }
            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                searchRunnable = () -> performSearch(currentSearchQuery, currentStatusFilter);
                handler.postDelayed(searchRunnable, 500); // 500ms debounce
            }
        });
    }

    private void setupTabs() {
        filterTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()) {
                    case 0: currentStatusFilter = "completed"; break;
                    case 1: currentStatusFilter = "pending"; break;
                    case 2: currentStatusFilter = "rejected"; break;
                }
                performSearch(currentSearchQuery, currentStatusFilter);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void performSearch(String query, String status) {
        showLoading();
        SharedPreferences prefs = requireActivity().getSharedPreferences("EZPrint_Prefs", Context.MODE_PRIVATE);
        int shopId = prefs.getInt("SHOP_ID", -1);

        if (shopId == -1) {
            updateUI(null, "Error: Could not find user session.");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.searchOrders(shopId, query, status).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body().getOrders();
                    if (orders.isEmpty()) {
                        updateUI(new ArrayList<>(), "No orders found.");
                    } else {
                        updateUI(orders, null);
                    }
                } else {
                    updateUI(null, "Failed to search orders.");
                }
            }
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e("SearchFragment", "API Failure: ", t);
                updateUI(null, "Network Error. Please try again.");
            }
        });
    }

    private void showLoading() {
        loadingSpinner.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        messageText.setVisibility(View.GONE);
    }

    private void updateUI(List<Order> orders, String message) {
        loadingSpinner.setVisibility(View.GONE);
        if (message != null) {
            searchResultsRecyclerView.setVisibility(View.GONE);
            messageText.setText(message);
            messageText.setVisibility(View.VISIBLE);
        } else {
            messageText.setVisibility(View.GONE);
            orderGridAdapter = new OrderGridAdapter(getContext(), orders);
            searchResultsRecyclerView.setAdapter(orderGridAdapter);
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}


