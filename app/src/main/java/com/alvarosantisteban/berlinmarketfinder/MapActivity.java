package com.alvarosantisteban.berlinmarketfinder;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.alvarosantisteban.berlinmarketfinder.model.Market;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
        List<Market> markets = getIntent().getParcelableArrayListExtra(ARG_MARKETS);

        if (markets != null) {
            for (Market market : markets) {
                LatLng marketPos = new LatLng(market.getLatitude(), market.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(marketPos).title(market.getName()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(marketPos));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            }
        }
    }
}

