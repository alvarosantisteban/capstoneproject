package com.alvarosantisteban.berlinmarketfinder.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The content provider to access the markets' DB.
 */
public class MarketsContentProvider extends ContentProvider {

    public static final int MARKETS = 100;
    public static final int MARKET_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MarketsContract.AUTHORITY, MarketsContract.PATH_MARKETS, MARKETS);
        uriMatcher.addURI(MarketsContract.AUTHORITY, MarketsContract.PATH_MARKETS +"/#", MARKET_WITH_ID);

        return uriMatcher;
    }

    private MarketsDbHelper marketsDbHelper;

    @Override
    public boolean onCreate() {
        marketsDbHelper = new MarketsDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase sqLiteDatabase = marketsDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor cursor;
        switch (match) {
            case MARKETS:
                cursor = sqLiteDatabase.query(MarketsContract.Market.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MARKET_WITH_ID:
                // URI has the following form: content://<authority>/market/# and we want to get the id
                String id = uri.getPathSegments().get(1);

                String theSelection = MarketsContract.Market.COLUMN_NAME_MARKET_ID +"=?";
                String[] theArgs = new String[]{id};

                cursor = sqLiteDatabase.query(MarketsContract.Market.TABLE_NAME,
                        projection,
                        theSelection,
                        theArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " +uri);
        }

        if(getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase sqLiteDatabase = marketsDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnedUri;
        switch (match) {
            case MARKETS:
                long id = sqLiteDatabase.insert(MarketsContract.Market.TABLE_NAME,
                        null,
                        values);
                if(id > 0) {
                    returnedUri = ContentUris.withAppendedId(MarketsContract.Market.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }

                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " +uri);
        }

        if(getContext() != null){
            // Notify the content resolver that a change has been made
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = marketsDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int numRowsDeleted;
        switch (match) {
            case MARKETS:
                numRowsDeleted = sqLiteDatabase.delete(MarketsContract.Market.TABLE_NAME,
                        "1",
                        null);

                break;
            case MARKET_WITH_ID:
                // URI has the following form: content://<authority>/market/# and we want to get the id
                String id = uri.getPathSegments().get(1);

                String theSelection = MarketsContract.Market.COLUMN_NAME_MARKET_ID +"=?";
                String[] theArgs = new String[]{id};

                numRowsDeleted = sqLiteDatabase.delete(MarketsContract.Market.TABLE_NAME,
                        theSelection,
                        theArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " +uri);
        }

        if(numRowsDeleted > 0 && getContext() != null) {
            // Notify the content resolver that a change has been made
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = marketsDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int numRowsUpdated;
        switch (match) {
            case MARKET_WITH_ID:
                // URI has the following form: content://<authority>/market/# and we want to get the id
                String id = uri.getPathSegments().get(1);

                String theSelection = MarketsContract.Market.COLUMN_NAME_MARKET_ID +"=?";
                String[] theArgs = new String[]{id};

                numRowsUpdated = sqLiteDatabase.update(MarketsContract.Market.TABLE_NAME,
                        values,
                        theSelection,
                        theArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " +uri);
        }

        if(numRowsUpdated > 0 && getContext() != null) {
            // Notify the content resolver that a change has been made
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case MARKETS:
                // directory
                return "vnd.android.cursor.dir" + "/" + MarketsContract.AUTHORITY + "/" + MarketsContract.PATH_MARKETS;
            case MARKET_WITH_ID:
                // single item type
                return "vnd.android.cursor.item" + "/" + MarketsContract.AUTHORITY + "/" + MarketsContract.PATH_MARKETS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
