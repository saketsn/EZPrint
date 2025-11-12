package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    // These names must match the keys in your JSON body
    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}

