package com.alvarosantisteban.berlinmarketfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.alvarosantisteban.berlinmarketfinder.model.Market;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Display a map with markers for the markets selected in MarketListActivity.
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String ARG_MARKETS = "markets";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        final List<Market> markets = getIntent().getParcelableArrayListExtra(ARG_MARKETS);
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapActivity.this, MarketDetailActivity.class);
                intent.putExtra(MarketDetailFragment.ARG_ITEM, getMarketFromId(markets, marker.getTag() != null ? (String) marker.getTag() : ""));
                startActivity(intent);
            }
        });

        if (markets != null) {
            for (Market market : markets) {
                LatLng marketPos = new LatLng(market.getLatitude(), market.getLongitude());
                Marker marker = googleMap.addMarker(new MarkerOptions().position(marketPos).title(market.getName()));
                marker.setTag(market.getId());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(marketPos));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            }
        }
    }

    @Nullable
    private Market getMarketFromId(@NonNull List<Market> marketsList, @NonNull String marketId) {
        for (Market market : marketsList) {
            if(marketId.equals(market.getId())) {
                return market;
            }
        }
        return null;
    }
}

