package com.alvarosantisteban.berlinmarketfinder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A helper class to manage database creation and version management of markets database.
 */
public class MarketsDbHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "markets.db";
    private final static int DATABASE_VERSION = 1;

    MarketsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREAT_MARKETS_TABLE = "CREATE TABLE " +
                MarketsContract.Market.TABLE_NAME + " (" +
                MarketsContract.Market._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MarketsContract.Market.COLUMN_NAME_MARKET_ID + " TEXT NOT NULL," +
                MarketsContract.Market.COLUMN_NAME_NEIGHBORHOOD + " TEXT NOT NULL," +
                MarketsContract.Market.COLUMN_NAME_NAME + " TEXT," +
                MarketsContract.Market.COLUMN_NAME_LATITUDE + " REAL," +
                MarketsContract.Market.COLUMN_NAME_LONGITUDE + " REAL," +
                MarketsContract.Market.COLUMN_NAME_OPENING_DAYS + " TEXT," +
                MarketsContract.Market.COLUMN_NAME_OPENING_HOURS + " TEXT," +
                MarketsContract.Market.COLUMN_NAME_ORGANIZER_NAME + " TEXT," +
                MarketsContract.Market.COLUMN_NAME_ORGANIZER_EMAIL + " TEXT," +
                MarketsContract.Market.COLUMN_NAME_ORGANIZER_WEBSITE + " TEXT," +
                MarketsContract.Market.COLUMN_NAME_OTHER_INFO + " TEXT" +
                ");";
        db.execSQL(SQL_CREAT_MARKETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Following code is commented because no changes are needed to the table so far, but if
        // they are needed in the future, they should follow the pattern proposed in the following
        // link: https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
//        if (oldVersion < 2) {
//            db.execSQL(DATABASE_ALTER_MOVIE_1);
//        }
//        if (oldVersion < 3) {
//            db.execSQL(DATABASE_ALTER_MOVIE_2);
//        }
    }
}
