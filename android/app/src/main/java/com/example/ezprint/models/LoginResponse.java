package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("ok")
    private boolean ok;

    @SerializedName("shop_id")
    private int shopId;

    @SerializedName("shop_name")
    private String shopName;

    @SerializedName("owner_name")
    private String ownerName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("address")
    private String address;

    @SerializedName("profile_img")
    private String profile_img;

    @SerializedName("qr_code_url")
    private String qr_code_url;

    public String getProfileImg() {
        return profile_img;
    }

    @SerializedName("message")
    private String message;

    // Getters to access the data
    public boolean isOk() {
        return ok;
    }

    public int getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getMessage() {
        return message;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getQrCodeUrl() {
        return qr_code_url;
    }
}

