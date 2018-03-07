package com.alvarosantisteban.berlinmarketfinder.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The container for the list of markets.
 */
public class MarketContainer {

    @SerializedName("index")
    @Expose
    private List<Market> markets;

    public MarketContainer(List<Market> markets) {
        this.markets = markets;
    }

    public List<Market> getMarkets() {
        return markets;
    }
}
