package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the JSON response from the QR code generation API.
 */
public class QrCodeResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("message")
    private String message;

    @SerializedName("qrCodeUrl")
    private String qrCodeUrl;

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public String getMessage() {
        return message;
    }
}

