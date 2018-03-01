package com.alvarosantisteban.berlinmarketfinder.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Market> ITEMS = new ArrayList<Market>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Market> ITEM_MAP = new HashMap<String, Market>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Market item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getId(), item);
    }

    private static Market createDummyItem(int position) {
        return new Market(String.valueOf(position),
                "Neukolln",
                "Market " + position,
                "52,466417",
                "13,437485",
                "Sa ganzj\\u00e4hrig\\nMarktbeginn: 02.04.2016",
                "10:00 - 16:00",
                "Pepito",
                "mailto:info@diemarktplaner.de",
                "www.dicke-linda-markt.de",
                makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about market ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
