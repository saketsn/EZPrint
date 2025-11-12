package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class OrderStatusUpdateRequest {

    @SerializedName("status")
    private final String status;

    @SerializedName("amount")
    private final Double amount; // Use Double (nullable) for optional field

    public OrderStatusUpdateRequest(String status, Double amount) {
        this.status = status;
        this.amount = amount;
    }
}
