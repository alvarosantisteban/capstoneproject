package com.alvarosantisteban.berlinmarketfinder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.alvarosantisteban.berlinmarketfinder.model.Market;

/**
 * Updates the Market widget so it displays the basic information of the last visited market.
 */
public class LastMarketProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, @Nullable Market market) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.last_market_provider);

        Intent intent;
        if (market != null) {
            views.setTextViewText(R.id.markets_list_name, market.getName());
            views.setTextViewText(R.id.markets_list_days, market.getOpeningDays());
            views.setTextViewText(R.id.markets_list_hours, market.getOpeningHours());

            intent = new Intent(context, MarketDetailActivity.class);
            intent.putExtra(MarketDetailFragment.ARG_ITEM, market);
        } else {
            views.setTextViewText(R.id.markets_list_name, context.getString(R.string.widget_default_info));

            intent = new Intent(context, MarketListActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }
    }

    public static void updateMarketWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int[] appWidgetIds, @Nullable Market market) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, market);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

