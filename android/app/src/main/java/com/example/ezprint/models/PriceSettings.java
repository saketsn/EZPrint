package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;

public class PriceSettings {
    @SerializedName("price_per_bw_page")
    private double pricePerBwPage;

    @SerializedName("price_per_color_page")
    private double pricePerColorPage;

    @SerializedName("price_per_bw_duplex")
    private double pricePerBwDuplex;

    @SerializedName("price_per_color_duplex")
    private double pricePerColorDuplex;

    // Getters
    public double getPricePerBwPage() { return pricePerBwPage; }
    public double getPricePerColorPage() { return pricePerColorPage; }
    public double getPricePerBwDuplex() { return pricePerBwDuplex; }
    public double getPricePerColorDuplex() { return pricePerColorDuplex; }

    // Constructor for making update requests
    public PriceSettings(double pricePerBwPage, double pricePerColorPage, double pricePerBwDuplex, double pricePerColorDuplex) {
        this.pricePerBwPage = pricePerBwPage;
        this.pricePerColorPage = pricePerColorPage;
        this.pricePerBwDuplex = pricePerBwDuplex;
        this.pricePerColorDuplex = pricePerColorDuplex;
    }
}

