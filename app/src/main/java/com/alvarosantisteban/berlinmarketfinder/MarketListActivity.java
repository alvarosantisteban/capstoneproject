package com.alvarosantisteban.berlinmarketfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.alvarosantisteban.berlinmarketfinder.data.MarketsContract;
import com.alvarosantisteban.berlinmarketfinder.model.Market;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Markets which are either downloaded or queried from the content
 * provider using an asynchronous task. The list of markets can be filtered by neighborhood.
 *
 * This activity has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MarketDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MarketListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MarketListActivity.class.getSimpleName();

    public static final int POS_ALL = 0;
    public static final String FILTER_ALL_NEIGHBORHOODS = "";
    private static final String SPINNER_POS = "spinnerPos";

    private enum DB_OPERATIONS {
        QUERY,
        REMOVE_AND_ADD
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private SimpleItemRecyclerViewAdapter adapter;
    private static int index = -1;
    private static int top = -1;
    
    // Used to distinguish between real user touches and automatic calls on onItemSelected
    private boolean hasUserTouchedSpinner = false;
    private Spinner spinner;
    private List<Market> markets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMapIntent = new Intent(MarketListActivity.this, MapActivity.class);
                goToMapIntent.putExtra(MapActivity.ARG_MARKETS, (ArrayList<? extends Parcelable>) markets);
                startActivity(goToMapIntent);
            }
        });

        if (findViewById(R.id.market_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        recyclerView = findViewById(R.id.market_list);
        layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int columnCount = mTwoPane || !getResources().getBoolean(R.bool.isTablet) ? 1 : 2;
        layoutManager.setSpanCount(columnCount);

        adapter = new SimpleItemRecyclerViewAdapter(this, null, mTwoPane, null);
        recyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Scroll to the last visited position of the recycler view
        if(index != -1) {
            layoutManager.scrollToPositionWithOffset(index, top);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the current recycler view's position
        index = layoutManager.findFirstVisibleItemPosition();
        View v = recyclerView.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // Reached from the bus when the list of markets that has been downloaded and parsed is available
    @Subscribe
    public void onMarketsDownloaded(List<Market> markets) {
        this.markets = markets;

        if(mTwoPane && markets.get(0) != null) {
            loadFragmentForMarket(markets.get(0));
        }

        // Delete all the markets with the ID of the new ones, and insert all the new markets
        new OperateWithDBAsyncTask(this, markets).execute(DB_OPERATIONS.REMOVE_AND_ADD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.markets_list_menu, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int spinnerPos = PreferenceManager.getDefaultSharedPreferences(this).getInt(SPINNER_POS, POS_ALL);
        spinner.setAdapter(adapter);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams((int) getResources().getDimension(R.dimen.spinner_width), ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setLayoutParams(lp);
        spinner.setGravity(Gravity.END);
        spinner.setSelection(spinnerPos);
        spinner.setOnItemSelectedListener(this);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hasUserTouchedSpinner = true;
                return v.performClick();
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                Log.d(TAG, "Download the markets from API");
                // Ask API for markets
                MarketsController marketsController = new MarketsController();
                marketsController.start(getCurrentFilter());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(!hasUserTouchedSpinner) {
            return;
        }
        hasUserTouchedSpinner = false;

        // Save the spinner position
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(SPINNER_POS, position);
        editor.apply();

        // Restart the loader to do the query for the right neighborhood
        getSupportLoaderManager().restartLoader(1, null, this);
    }

    @NonNull
    private String getCurrentFilter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int currentPos = sharedPreferences.getInt(SPINNER_POS, POS_ALL);

        return filterBySpinnerPos(currentPos);
    }

    @NonNull
    private String filterBySpinnerPos(int position) {
        // We are avoiding on purpose returning the string for position=0 because the string that
        // it returns is "All", which is not what we want to send in the API request
        if(position > 0 && position < getResources().getStringArray(R.array.sort_order).length) {
            return getResources().getStringArray(R.array.sort_order)[position];
        } else {
            return FILTER_ALL_NEIGHBORHOODS;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(spinner != null) {
            // Save the spinner position
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt(SPINNER_POS, spinner.getSelectedItemPosition());
            editor.apply();
        }
    }

    void loadFragmentForMarket(@NonNull Market market) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(MarketDetailFragment.ARG_ITEM, market);
        MarketDetailFragment fragment = new MarketDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.market_detail_container, fragment)
                .commit();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // ASYNC TASK FOR CONTENT PROVIDER
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper class to do operations with the content provider in an asynchronous thread.
     */
    private static class OperateWithDBAsyncTask extends AsyncTask<DB_OPERATIONS, Void, Cursor> {

        @Nullable
        private final List<Market> marketList;
        private DB_OPERATIONS dbOperation;
        @NonNull
        private WeakReference<MarketListActivity> activityReference;

        OperateWithDBAsyncTask(@NonNull MarketListActivity context, @Nullable List<Market> marketList) {
            this.marketList = marketList;
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Cursor doInBackground(DB_OPERATIONS... dbOperations) {
            // Check if the activity is there
            MarketListActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            dbOperation = dbOperations[0];
            switch (dbOperation) {
                case REMOVE_AND_ADD:
                    if (marketList != null) {
                        Util.deleteMarkets(activity, marketList);
                        Util.insertMarketsInDB(activity, marketList);
                    }
                    return null;
            }
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // LOADER CALLBACKS
    ////////////////////////////////////////////////////////////////////////////////

    @NonNull
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        String neighborhood = getCurrentFilter();
        String selection;
        String[] selectionArgs = {neighborhood};
        if (neighborhood.contains(getResources().getStringArray(R.array.sort_order)[13])) {
            // The string for Brandenburg usually contains the name of the city too: Brandenburg (Potsdam)
            selection = MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD + " LIKE 'Brandenburg%'";
            selectionArgs = null;
        } else if (neighborhood.equals("")) {
            selection = null;
            selectionArgs = null;
        } else {
            selection = MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD + "=?";
        }

        return new CursorLoader(this, MarketsContract.Market.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {

        if(cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "Retrieve the markets from DB");

            markets = Util.getMarketsFromCursor(cursor);
            // Display the first market in two pane mode
            if(mTwoPane && markets.get(0) != null) {
                loadFragmentForMarket(markets.get(0));
            }

            adapter.swapCursor(cursor);
        } else {
            Log.d(TAG, "Download the markets from API");
            // No entries, ask to API
            MarketsController marketsController = new MarketsController();
            marketsController.start(getCurrentFilter());
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.d(TAG, "onLoaderReset");
    }
}
