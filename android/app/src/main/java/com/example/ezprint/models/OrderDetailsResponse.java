package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

/**
 * Wrapper class for the /orders/details/:orderId API response.
 */
public class OrderDetailsResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("order")
    private Order order;

    public Order getOrder() {
        return order;
    }
}
