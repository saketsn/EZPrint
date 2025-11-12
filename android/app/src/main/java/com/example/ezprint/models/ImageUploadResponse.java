package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class ImageUploadResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("message")
    private String message;

    @SerializedName("imageUrl")
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }
}
