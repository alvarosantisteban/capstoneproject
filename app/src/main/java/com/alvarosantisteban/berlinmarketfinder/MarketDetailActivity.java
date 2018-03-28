package com.alvarosantisteban.berlinmarketfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.alvarosantisteban.berlinmarketfinder.model.Market;

/**
 * An activity representing a single Market detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MarketListActivity}.
 */
public class MarketDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Market market = getIntent().getParcelableExtra(MarketDetailFragment.ARG_ITEM);
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(MarketDetailFragment.ARG_ITEM, market);
            MarketDetailFragment fragment = new MarketDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.market_detail_container, fragment)
                    .commit();
        }
        ImageView cover = findViewById(R.id.market_cover);
        cover.setImageResource(Util.getCoverImage(market.getNeighborhood(), this));
        CollapsingToolbarLayout appBarLayout = findViewById(R.id.collapsing_toolbar);
        appBarLayout.setTitle(market.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, MarketListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void shareMarketDetails(View view) {
        Market market = getIntent().getParcelableExtra(MarketDetailFragment.ARG_ITEM);
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(getString(R.string.share_text_tag, market.getName()))
                .getIntent(), getString(R.string.action_share)));
    }
}
