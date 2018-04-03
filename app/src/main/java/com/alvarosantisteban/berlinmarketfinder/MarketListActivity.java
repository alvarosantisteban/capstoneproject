package com.alvarosantisteban.berlinmarketfinder;

import android.content.ContentValues;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
public class MarketListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

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
    private int columnCount;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
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
        columnCount = mTwoPane || !getResources().getBoolean(R.bool.isTablet) ? 1 : 2;
        layoutManager.setSpanCount(columnCount);

        // Throw an async task to ask the content provider for all the markets in the DB
        new OperateWithDBAsyncTask(this).execute(DB_OPERATIONS.QUERY);
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

        // Set the markets in the recycler view
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, markets, mTwoPane));
        this.layoutManager = new GridLayoutManager(this, columnCount);
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
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
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

        new OperateWithDBAsyncTask(this).execute(DB_OPERATIONS.QUERY);
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

    private void loadFragmentForMarket(@NonNull Market market) {
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

        OperateWithDBAsyncTask(@NonNull MarketListActivity context) {
            this.marketList = null;
            activityReference = new WeakReference<>(context);
        }

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
                        deleteMarkets(activity, marketList);
                        insertMarketsInDB(activity, marketList);
                    }
                    return null;
                case QUERY:
                    return queryMarkets(activity, activity.getCurrentFilter());

            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable Cursor cursor) {
            super.onPostExecute(cursor);

            MarketListActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            switch (dbOperation) {
                case QUERY:
                    if(cursor != null && cursor.getCount() > 0) {
                        activity.markets = getMarketsFromCursor(cursor);

                        activity.recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(activity, activity.markets, activity.mTwoPane));
                        if(activity.mTwoPane && activity.markets.get(0) != null) {
                            activity.loadFragmentForMarket(activity.markets.get(0));
                        }
                    } else {
                        // No entries, ask to API
                        MarketsController marketsController = new MarketsController();
                        marketsController.start(activity.getCurrentFilter());
                    }
                case REMOVE_AND_ADD:

            }
        }

        /**
         * Creates a list of Markets from a Cursor.
         */
        @NonNull
        private List<Market> getMarketsFromCursor(Cursor cursor) {
            List<Market> markets = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Market market = new Market(cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_MARKET_ID)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_NAME)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_LATITUDE)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_OPENING_DAYS)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_OPENING_HOURS)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_ORGANIZER_NAME)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_ORGANIZER_EMAIL)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_ORGANIZER_WEBSITE)),
                            cursor.getString(cursor.getColumnIndex(MarketsContract.Market.COLUMN_NAME_OTHER_INFO)));
                    markets.add(market);
                }
            } finally {
                cursor.close();
            }
            return markets;
        }

        private void insertMarketsInDB(@NonNull MarketListActivity activity, @NonNull List<Market> markets) {
            ContentValues[] contentValues = new ContentValues[markets.size()];
            Market market;
            for (int i = 0; i < markets.size(); i++) {
                market = markets.get(i);
                ContentValues values = new ContentValues();
                values.put(MarketsContract.Market.COLUMN_NAME_MARKET_ID, market.getId());
                values.put(MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD, market.getNeighborhood());
                values.put(MarketsContract.Market.COLUMN_NAME_NAME, market.getName());
                values.put(MarketsContract.Market.COLUMN_NAME_LATITUDE, market.getLatitudeString());
                values.put(MarketsContract.Market.COLUMN_NAME_LONGITUDE, market.getLongitudeString());
                values.put(MarketsContract.Market.COLUMN_NAME_OPENING_DAYS, market.getOpeningDays());
                values.put(MarketsContract.Market.COLUMN_NAME_OPENING_HOURS, market.getOpeningHours());
                values.put(MarketsContract.Market.COLUMN_NAME_ORGANIZER_NAME, market.getOrganizerNameAndPhone());
                values.put(MarketsContract.Market.COLUMN_NAME_ORGANIZER_EMAIL, market.getOrganizerEmail());
                values.put(MarketsContract.Market.COLUMN_NAME_ORGANIZER_WEBSITE, market.getOrganizerWebsite());
                values.put(MarketsContract.Market.COLUMN_NAME_OTHER_INFO, market.getOtherInfo());
                contentValues[i] = values;
            }

            activity.getContentResolver().bulkInsert(MarketsContract.Market.CONTENT_URI, contentValues);
        }

        private void deleteMarkets(@NonNull MarketListActivity activity, @NonNull List<Market> markets) {
            for (Market market : markets) {
                activity.getContentResolver().delete(MarketsContract.Market.CONTENT_URI.buildUpon().appendPath(market.getId()).build(),
                        null,
                        null);
            }
        }

        private Cursor queryMarkets(@NonNull MarketListActivity activity, @Nullable String neighborhood) {
            String selection;
            String[] selectionArgs = {neighborhood};
            if (neighborhood != null && neighborhood.contains(activity.getResources().getStringArray(R.array.sort_order)[13])) {
                // The string for Brandenburg usually contains the name of the city too: Brandenburg (Potsdam)
                selection = MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD + " LIKE 'Brandenburg%'";
                selectionArgs = null;
            } else {
                selection = MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD + "=?";
            }

            return activity.getContentResolver().query(MarketsContract.Market.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // ADAPTER FOR RECYCLER VIEW
    ////////////////////////////////////////////////////////////////////////////////

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final MarketListActivity mParentActivity;
        private final List<Market> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Market market = (Market) view.getTag();
                if (mTwoPane) {
                    mParentActivity.loadFragmentForMarket(market);
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, MarketDetailActivity.class);
                    intent.putExtra(MarketDetailFragment.ARG_ITEM, market);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(MarketListActivity parent,
                                      List<Market> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.market_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.neighborHoodImage.setImageResource(Util.getCoverImage(mValues.get(position).getNeighborhood(), mParentActivity));

            holder.neighborhood.setText(mValues.get(position).getNeighborhood());
            holder.name.setText(mValues.get(position).getName());
            holder.openDays.setText(mValues.get(position).getOpeningDays());
            holder.openHours.setText(mValues.get(position).getOpeningHours());
            if (mTwoPane) {
                // If we are in two pane mode, do not display the opening days and hours and give more
                // space to the market's name
                holder.name.setMaxLines(4);
                holder.openDays.setVisibility(View.GONE);
                holder.openHours.setVisibility(View.GONE);
            }

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView neighborHoodImage;
            final TextView neighborhood;
            final TextView name;
            final TextView openDays;
            final TextView openHours;

            ViewHolder(View view) {
                super(view);
                neighborHoodImage = view.findViewById(R.id.market_cover);
                neighborhood = view.findViewById(R.id.markets_list_neighborhood);
                name = view.findViewById(R.id.markets_list_name);
                openDays = view.findViewById(R.id.markets_list_days);
                openHours = view.findViewById(R.id.markets_list_hours);
            }
        }
    }
}
