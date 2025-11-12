package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the JSON body for the register-device API call.
 */
public class RegisterDeviceRequest {
    @SerializedName("shopId")
    private final int shopId;

    @SerializedName("fcmToken")
    private final String fcmToken;

    public RegisterDeviceRequest(int shopId, String fcmToken) {
        this.shopId = shopId;
        this.fcmToken = fcmToken;
    }
}

