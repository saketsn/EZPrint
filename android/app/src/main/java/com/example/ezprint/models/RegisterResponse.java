package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    @SerializedName("ok")
    private boolean ok;

    @SerializedName("shop_id")
    private int shopId;

    @SerializedName("message")
    private String message;

    public boolean isOk() {
        return ok;
    }

    public int getShopId() {
        return shopId;
    }

    public String getMessage() {
        return message;
    }
}
