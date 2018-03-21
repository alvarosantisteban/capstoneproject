package com.alvarosantisteban.berlinmarketfinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alvarosantisteban.berlinmarketfinder.model.Market;

/**
 * A fragment representing a single Market detail screen.
 * This fragment is either contained in a {@link MarketListActivity}
 * in two-pane mode (on tablets) or a {@link MarketDetailActivity}
 * on handsets.
 */
public class MarketDetailFragment extends Fragment {

    public static final String ARG_ITEM = "item";

    private Market market;

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

            Activity activity = this.getActivity();
            assert activity != null;
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(market.getName());
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.market_detail, container, false);

        if (market != null) {
            ((TextView) rootView.findViewById(R.id.market_detail)).setText(market.getOtherInfo());
        }

        return rootView;
    }
}
