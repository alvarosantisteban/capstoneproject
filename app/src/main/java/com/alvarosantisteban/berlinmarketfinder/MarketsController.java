package com.alvarosantisteban.berlinmarketfinder;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alvarosantisteban.berlinmarketfinder.api.BerlinMarketsAPI;
import com.alvarosantisteban.berlinmarketfinder.model.MarketContainer;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Creates the Retrofit client, calls the TheMovieDB API for the specific endpoint and posts the
 * result in the Otto bus.
 */
class MarketsController implements Callback<MarketContainer> {

    private static final String TAG = MarketsController.class.getSimpleName();

    void start() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BerlinMarketsAPI.BERLIN_MARKETS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();

        BerlinMarketsAPI berlinMarketsAPI = retrofit.create(BerlinMarketsAPI.class);

        Call<MarketContainer> call = berlinMarketsAPI.getMarkets(""); // FIXME Allow filtering by neighborhood
        call.enqueue(this);
    }

    @Override
    public void onResponse(@NonNull Call<MarketContainer> call, @NonNull Response<MarketContainer> response) {
        if (response.isSuccessful()) {
            MarketContainer markets = response.body();
            if (markets != null && markets.getMarkets() != null) {
                EventBus.getDefault().post(markets.getMarkets());
            } else {
                Log.e(TAG, "Markets are null");
            }
        } else {
            Log.e(TAG, response.errorBody().toString());
        }
    }

    @Override
    public void onFailure(@NonNull Call<MarketContainer> call,@NonNull Throwable t) {
        Log.e(TAG, "onFailure: " +t.toString());
    }
}

