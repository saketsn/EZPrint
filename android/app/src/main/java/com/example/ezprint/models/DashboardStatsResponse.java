package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class DashboardStatsResponse {

    @SerializedName("ok")
    private boolean ok;

    @SerializedName("stats")
    private DashboardStats stats;

    public DashboardStats getStats() {
        return stats;
    }

    public boolean isOk() {
        return ok;
    }
}
