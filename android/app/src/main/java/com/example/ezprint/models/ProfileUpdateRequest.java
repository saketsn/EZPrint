package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateRequest {
    @SerializedName("shop_name")
    private final String shopName;
    @SerializedName("owner_name")
    private final String ownerName;
    @SerializedName("email")
    private final String email;
    @SerializedName("phone")
    private final String phone;
    @SerializedName("address")
    private final String address;

    public ProfileUpdateRequest(String shopName, String ownerName, String email, String phone, String address) {
        this.shopName = shopName;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
