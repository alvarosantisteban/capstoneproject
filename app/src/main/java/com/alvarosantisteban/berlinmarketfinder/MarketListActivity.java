package com.alvarosantisteban.berlinmarketfinder;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
 * provider using an asynchronous task.
 *
 * This activity has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MarketDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MarketListActivity extends AppCompatActivity {

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        // Throw an async task to ask the content provider for all the markets in the DB
        new OperateWithDBAsyncTask(this).execute(DB_OPERATIONS.QUERY);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // Reached from the bus when the list of markets that has been downloaded and parsed is available
    @Subscribe
    public void onMarketsDownloaded(List<Market> markets) {
        // Set the markets in the recycler view
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, markets, mTwoPane));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Delete all the markets with the ID of the new ones, and insert all the new markets
        new OperateWithDBAsyncTask(this, markets).execute(DB_OPERATIONS.REMOVE_AND_ADD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.markets_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                // Ask API for markets
                MarketsController marketsController = new MarketsController();
                marketsController.start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                    // TODO Probably separate these two operations
                    if (marketList != null) {
                        deleteMarkets(activity, marketList);
                        insertMarketsInDB(activity, marketList);
                    }
                    return null;
                case QUERY:
                    return queryMarkets(activity);

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
                        List<Market> markets = getMarketsFromCursor(cursor);

                        activity.recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(activity, markets, activity.mTwoPane));
                        activity.recyclerView.addItemDecoration(new DividerItemDecoration(activity.recyclerView.getContext(), DividerItemDecoration.VERTICAL));
                    } else {
                        // No entries, ask to API
                        MarketsController marketsController = new MarketsController();
                        marketsController.start();
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
                values.put(MarketsContract.Market.COLUMN_NAME_LATITUDE, market.getLatitude());
                values.put(MarketsContract.Market.COLUMN_NAME_LONGITUDE, market.getLongitude());
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

        private Cursor queryMarkets(@NonNull MarketListActivity activity) {
            return activity.getContentResolver().query(MarketsContract.Market.CONTENT_URI, null, null, null, null, null);
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
                Market item = (Market) view.getTag();
                // TODO Make changes to pass the Market, not just the ID
                if (mTwoPane) {
//                    Bundle arguments = new Bundle();
//                    arguments.putString(MarketDetailFragment.ARG_ITEM_ID, item.getId());
//                    MarketDetailFragment fragment = new MarketDetailFragment();
//                    fragment.setArguments(arguments);
//                    mParentActivity.getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.market_detail_container, fragment)
//                            .commit();
                } else {
//                    Context context = view.getContext();
//                    Intent intent = new Intent(context, MarketDetailActivity.class);
//                    intent.putExtra(MarketDetailFragment.ARG_ITEM_ID, item.getId());
//
//                    context.startActivity(intent);
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
            holder.neighborhood.setText(mValues.get(position).getNeighborhood());
            holder.name.setText(mValues.get(position).getName());
            holder.openDays.setText(mValues.get(position).getOpeningDays());
            holder.openHours.setText(mValues.get(position).getOpeningHours());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView neighborhood;
            final TextView name;
            final TextView openDays;
            final TextView openHours;

            ViewHolder(View view) {
                super(view);
                neighborhood = view.findViewById(R.id.markets_list_neighborhood);
                name = view.findViewById(R.id.markets_list_name);
                openDays = view.findViewById(R.id.markets_list_days);
                openHours = view.findViewById(R.id.markets_list_hours);
            }
        }
    }
}
