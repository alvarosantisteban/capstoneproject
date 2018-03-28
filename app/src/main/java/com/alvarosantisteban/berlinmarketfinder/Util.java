package com.alvarosantisteban.berlinmarketfinder;

import android.content.Context;
import android.support.annotation.NonNull;

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
}
