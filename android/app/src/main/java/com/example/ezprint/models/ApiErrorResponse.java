package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the structure of a JSON error response from the API.
 * e.g., {"error": "Invalid password"}
 */
public class ApiErrorResponse {
    @SerializedName("error")
    private String error;

    public String getError() {
        return error;
    }
}
