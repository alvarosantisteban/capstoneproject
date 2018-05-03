package com.alvarosantisteban.berlinmarketfinder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvarosantisteban.berlinmarketfinder.model.Market;

import java.util.List;

/**
 * An adapter that works both with a {@link Cursor} or a {@link List} of {@link Market}.
 */
public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = SimpleItemRecyclerViewAdapter.class.getSimpleName();

    private final MarketListActivity mParentActivity;
    private List<Market> mValues;
    private Cursor cursor;
    private boolean useCursor;
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
                                  boolean twoPane, @Nullable Cursor cursor) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
        this.cursor = cursor;
        useCursor = true;
    }

    @NonNull
    @Override
    public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.market_list_content, parent, false);
        return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
        if(useCursor) {
            if (cursor != null) {
                cursor.moveToPosition(position);
                Market market = Market.from(cursor);

                holder.neighborHoodImage.setImageResource(Util.getCoverImage(market.getNeighborhood(), mParentActivity));

                holder.neighborhood.setText(market.getNeighborhood());
                holder.name.setText(market.getName());
                holder.openDays.setText(market.getOpeningDays());
                holder.openHours.setText(market.getOpeningHours());

                holder.itemView.setTag(market);
                holder.itemView.setOnClickListener(mOnClickListener);
            }
        }else {
            holder.neighborHoodImage.setImageResource(Util.getCoverImage(mValues.get(position).getNeighborhood(), mParentActivity));

            holder.neighborhood.setText(mValues.get(position).getNeighborhood());
            holder.name.setText(mValues.get(position).getName());
            holder.openDays.setText(mValues.get(position).getOpeningDays());
            holder.openHours.setText(mValues.get(position).getOpeningHours());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        if (mTwoPane) {
            // If we are in two pane mode, do not display the opening days and hours and give more
            // space to the market's name
            holder.name.setMaxLines(4);
            holder.openDays.setVisibility(View.GONE);
            holder.openHours.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (useCursor) {
            return cursor != null ? cursor.getCount() : 0;
        } else {
            return mValues.size();
        }
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

    void swapCursor(Cursor newCursor) {
        Log.d(TAG, "swapCursor");
        useCursor = true;
        cursor = newCursor;
        // After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }

    void useListInsteadOfCursor(List<Market> newMarkets) {
        useCursor = false;
        mValues = newMarkets;
        // After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }
}
