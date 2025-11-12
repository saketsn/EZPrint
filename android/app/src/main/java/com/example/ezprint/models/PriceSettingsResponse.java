package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

// Wrapper class for the API response
public class PriceSettingsResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("prices")
    private PriceSettings prices;

    public PriceSettings getPrices() {
        return prices;
    }
}