package com.alvarosantisteban.berlinmarketfinder.api;

import com.alvarosantisteban.berlinmarketfinder.model.MarketContainer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * The interface to communicate with the Berlin.de's Second-hand markets' API using Retrofit.
 */
public interface BerlinMarketsAPI {

    String BERLIN_MARKETS_BASE_URL = "http://www.berlin.de/sen/web/service/maerkte-feste/wochen-troedelmaerkte/index.php/";

    @GET("index/index.json")
    Call<MarketContainer> getMarkets(@Query("bezirk") String neighborhood, @Query("ipp") String limit);
}
