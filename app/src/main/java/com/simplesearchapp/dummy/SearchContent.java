package com.simplesearchapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SearchContent {

    /**
     * An array of sample (dummy) items.
     */
    public static ArrayList<SearchItem> ITEMS = new ArrayList<SearchItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, SearchItem> ITEM_MAP = new HashMap<String, SearchItem>();

    //private static final int COUNT = 25;

/*    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }*/

    public static void addItem(SearchItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

/*    private static SearchContent createDummyItem(int position) {
        return new SearchContent(String.valueOf(position), "Item " + position, makeDetails(position));
    }*/

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class SearchItem {
        public String id;
        public String name;
        public String description;
        public String imageurl;

        public SearchItem(String id, String name, String description, String imageurl) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.imageurl = imageurl;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
