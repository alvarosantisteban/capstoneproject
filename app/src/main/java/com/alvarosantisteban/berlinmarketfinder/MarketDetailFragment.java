package com.alvarosantisteban.berlinmarketfinder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvarosantisteban.berlinmarketfinder.model.Market;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A fragment representing a single Market detail screen.
 * This fragment is either contained in a {@link MarketListActivity}
 * in two-pane mode (on tablets) or a {@link MarketDetailActivity}
 * on handsets.
 */
public class MarketDetailFragment extends Fragment {

    public static final String ARG_ITEM = "item";
    private static final String KEY_SAVED_MARKET = "savedMarket";

    private GoogleMap map;
    private Market market;
    private MapView mapView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MarketDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM)) {
            market = getArguments().getParcelable(ARG_ITEM);
        } else if(savedInstanceState.containsKey(KEY_SAVED_MARKET)) {
            market = savedInstanceState.getParcelable(KEY_SAVED_MARKET);
        } else {
            throw new IllegalStateException("There is no market neither from the arguments or savedInstance");
        }

        Activity activity = this.getActivity();
        assert activity != null;
        CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.collapsing_toolbar);
        if (appBarLayout != null) {
            appBarLayout.setTitle(market.getName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.market_detail, container, false);

        mapView = rootView.findViewById(R.id.market_mapView);
        mapView.onCreate(savedInstanceState);
        if (market != null) {
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;

                    LatLng marketPos = new LatLng(market.getLatitude(), market.getLongitude());
                    map.addMarker(new MarkerOptions().position(marketPos).title(market.getName()));
                    map.moveCamera(CameraUpdateFactory.newLatLng(marketPos));
                    map.animateCamera(CameraUpdateFactory.zoomTo(14));
                }
            });

            ((TextView) rootView.findViewById(R.id.market_name)).setText(market.getName());
            ((TextView) rootView.findViewById(R.id.market_opening_days)).setText(market.getOpeningDays());
            ((TextView) rootView.findViewById(R.id.market_opening_hours)).setText(market.getOpeningHours());
            setTextViewIfNonEmpty((TextView) rootView.findViewById(R.id.market_other_info),
                    market.getOtherInfo(),
                    (TextView) rootView.findViewById(R.id.market_other_info_tag));
            setTextViewIfNonEmpty((TextView) rootView.findViewById(R.id.market_contact_info_name_and_phone),
                    market.getOrganizerNameAndPhone(),
                    (TextView) rootView.findViewById(R.id.market_contact_info_tag));
            ImageView website = rootView.findViewById(R.id.market_contact_info_website);
            ImageView email = rootView.findViewById(R.id.market_contact_info_email);
            displayImageViewIfNonEmpty(website, market.getOrganizerWebsite());
            displayImageViewIfNonEmpty(email, market.getOrganizerEmail());
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SAVED_MARKET, market);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void setTextViewIfNonEmpty(@NonNull TextView view, @Nullable String text, @Nullable TextView relatedTag) {
        if (text != null && !text.equals("")) {
            view.setText(text);
        } else {
            view.setVisibility(View.GONE);
            if (relatedTag != null) {
                relatedTag.setVisibility(View.GONE);
            }
        }
    }

    private void displayImageViewIfNonEmpty(@NonNull ImageView view, @Nullable final String link) {
        if (link != null && !link.equals("")) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if(link.startsWith("http")) {
                        intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(link));
                    } else {
                        intent = new Intent(Intent.ACTION_SENDTO,
                                Uri.parse(link));
                    }
                    startActivity(intent);
                }
            });
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
