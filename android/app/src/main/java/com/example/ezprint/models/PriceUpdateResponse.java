package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the structure of a successful JSON response from the price update API.
 * e.g., {"ok": true, "message": "Prices updated successfully."}
 */
public class PriceUpdateResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public boolean isOk() {
        return ok;
    }
}
