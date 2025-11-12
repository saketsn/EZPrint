package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class DashboardStats {
    @SerializedName("todays_total_orders")
    private int todaysTotalOrders;
    @SerializedName("pending_orders")
    private int pendingOrders;
    @SerializedName("in_progress_orders")
    private int inProgressOrders;
    @SerializedName("completed_orders")
    private int completedOrders;
    @SerializedName("todays_revenue")
    private double todaysRevenue;
    @SerializedName("monthly_orders")
    private int monthlyOrders;
    @SerializedName("new_customers")
    private int newCustomers;
    @SerializedName("revenue_change_percentage")
    private double revenueChangePercentage;

    // Getters for all fields...

    public int getTodaysTotalOrders() {
        return todaysTotalOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public int getInProgressOrders() {
        return inProgressOrders;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public double getTodaysRevenue() {
        return todaysRevenue;
    }

    public int getMonthlyOrders() {
        return monthlyOrders;
    }

    public int getNewCustomers() {
        return newCustomers;
    }

    public double getRevenueChangePercentage() {
        return revenueChangePercentage;
    }
}
