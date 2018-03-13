package com.alvarosantisteban.berlinmarketfinder;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Custom application to have Stetho.
 */
public class MarketApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
