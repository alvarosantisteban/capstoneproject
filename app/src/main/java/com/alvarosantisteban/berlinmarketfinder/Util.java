package com.alvarosantisteban.berlinmarketfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.alvarosantisteban.berlinmarketfinder.data.MarketsContract;
import com.alvarosantisteban.berlinmarketfinder.model.Market;

import java.util.ArrayList;
import java.util.List;

/**
 * Small class for methods used all around the app.
 */
final class Util {

    /**
     * Returns the image's resource id corresponding to the neighborhood passed by parameter.
     */
    static int getCoverImage(@NonNull String neighborhood, @NonNull Context context) {
        int neighborhoodImageId = 0;
        String[] neighborhoods = context.getResources().getStringArray(R.array.sort_order);
        if (neighborhood.equals(neighborhoods[1])) {
            neighborhoodImageId = R.drawable.neighborhoods_01_charlottenburg;
        } else if (neighborhood.equals(neighborhoods[2])) {
            neighborhoodImageId = R.drawable.neighborhoods_02_fhain_kreuzberg;
        } else if (neighborhood.equals(neighborhoods[3])) {
            neighborhoodImageId = R.drawable.neighborhoods_03_lichtenberg;
        } else if (neighborhood.equals(neighborhoods[4])) {
            neighborhoodImageId = R.drawable.neighborhoods_04_marzahn;
        } else if (neighborhood.equals(neighborhoods[5])) {
            neighborhoodImageId = R.drawable.neighborhoods_05_mitte;
        } else if (neighborhood.equals(neighborhoods[6])) {
            neighborhoodImageId = R.drawable.neighborhoods_06_neukoelln;
        } else if (neighborhood.equals(neighborhoods[7])) {
            neighborhoodImageId = R.drawable.neighborhoods_07_pankow;
        } else if (neighborhood.equals(neighborhoods[8])) {
            neighborhoodImageId = R.drawable.neighborhoods_08_reinickendorf;
        } else if (neighborhood.equals(neighborhoods[9])) {
            neighborhoodImageId = R.drawable.neighborhoods_09_spandau;
        } else if (neighborhood.equals(neighborhoods[10])) {
            neighborhoodImageId = R.drawable.neighborhoods_10_steglitz;
        } else if (neighborhood.equals(neighborhoods[11])) {
            neighborhoodImageId = R.drawable.neighborhoods_11_schoeneberg;
        } else if (neighborhood.equals(neighborhoods[12])) {
            neighborhoodImageId = R.drawable.neighborhoods_12_treptow;
        } else if (neighborhood.contains(neighborhoods[13])) { // The string for Brandenburg usually contains the name of the city too: Brandenburg (Potsdam)
            neighborhoodImageId = R.drawable.neighborhoods_13_brandenburg;
        }
        return neighborhoodImageId;
    }

    static void insertMarketsInDB(@NonNull MarketListActivity activity, @NonNull List<Market> markets) {
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

    static void deleteMarkets(@NonNull MarketListActivity activity, @NonNull List<Market> markets) {
        for (Market market : markets) {
            activity.getContentResolver().delete(MarketsContract.Market.CONTENT_URI.buildUpon().appendPath(market.getId()).build(),
                    null,
                    null);
        }
    }

    /**
     * Creates a list of Markets from a Cursor.
     */
    @NonNull
    static List<Market> getMarketsFromCursor(Cursor cursor) {
        List<Market> markets = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                markets.add(Market.from(cursor));
            }
        } finally {
            // Put the cursor back in the first position
            cursor.moveToFirst();
        }
        return markets;
    }

}
