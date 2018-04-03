package com.alvarosantisteban.berlinmarketfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.alvarosantisteban.berlinmarketfinder.model.Market;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Display a map with markers for the markets selected in MarketListActivity.
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    public static final String ARG_MARKETS = "markets";
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final double BERLIN_DEFAULT_LAT = 52.496511;
    private static final double BERLIN_DEFAULT_LNG = 13.367152;
    private static final int DEFAULT_CAMERA_ZOOM = 12;


    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap googleMap;
    private Marker userMarker;
    private boolean hasPermissionDialogBeenDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpUserLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setUpUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null && googleMap != null) {
                        if (userMarker == null) {
                            // Create a new marker for the user
                            userMarker = googleMap.addMarker(new MarkerOptions().
                                    position(new LatLng(location.getLatitude(), location.getLongitude())).
                                    title(getString(R.string.map_activity_user_marker_title)).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        } else {
                            // Update the user's marker
                            userMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    }
                }
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMapLoadedCallback(this);

        final List<Market> markets = getIntent().getParcelableArrayListExtra(ARG_MARKETS);

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() != null && !marker.getTag().equals("")) {
                    Intent intent = new Intent(MapActivity.this, MarketDetailActivity.class);
                    intent.putExtra(MarketDetailFragment.ARG_ITEM, getMarketFromId(markets, (String) marker.getTag()));
                    startActivity(intent);
                }
            }
        });

        if (markets != null) {
            LatLng marketPos = new LatLng(BERLIN_DEFAULT_LAT, BERLIN_DEFAULT_LNG);
            for (Market market : markets) {
                marketPos = new LatLng(market.getLatitude(), market.getLongitude());
                if(marketPos.latitude == -1 || marketPos.longitude == -1) continue; // If pos invalid, do not add the marker
                Marker marker = googleMap.addMarker(new MarkerOptions().position(marketPos).title(market.getName()));
                marker.setTag(market.getId());
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(marketPos));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM));
        }
    }

    @Override
    public void onMapLoaded() {
        // This is needed because in some devices the camera can't make movements until this callback
        // is reached, in which case the latitude is around 10 and all the markers for Berlin are greater
        // than 50
        if(googleMap.getCameraPosition().target.latitude < 50) {
            LatLng marketPos = new LatLng(BERLIN_DEFAULT_LAT, BERLIN_DEFAULT_LNG);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(marketPos));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM));
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }
            }
        }
    }

    private void startLocationUpdates() {
        if(!hasPermissionDialogBeenDisplayed) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_CODE);
                hasPermissionDialogBeenDisplayed = true;
                return;
            }
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);
        }
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
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

