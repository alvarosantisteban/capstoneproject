package com.alvarosantisteban.berlinmarketfinder.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract that specifies the layout of how the DB of the app is organised.
 */
public final class MarketsContract {

    static final String AUTHORITY = "com.alvarosantisteban.berlinmarketfinder";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" +AUTHORITY);

    static final String PATH_MARKETS = "market";

    private MarketsContract(){
        throw new AssertionError("This class should not be instanced");
    }

    public static class Market implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MARKETS).build();

        static final String TABLE_NAME = "market";

        public static final String COLUMN_NAME_MARKET_ID= "marketId";
        public static final String COLUMN_NAME_NEIGHBORHOOD = "neighborhood";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LATITUDE= "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_OPENING_DAYS = "openingDays";
        public static final String COLUMN_NAME_OPENING_HOURS = "openingHours";
        public static final String COLUMN_NAME_ORGANIZER_NAME= "organizerName";
        public static final String COLUMN_NAME_ORGANIZER_EMAIL = "organizerEmail";
        public static final String COLUMN_NAME_ORGANIZER_WEBSITE = "organizerWebsite";
        public static final String COLUMN_NAME_OTHER_INFO = "otherInfo";
    }
}
