package com.example.autopalette.client.util;

import java.util.HashSet;
import java.util.Set;

public class MaterialHighlighter {
    private static boolean active = false;
    private static final Set<String> neededItemIds = new HashSet<>();

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean isActive) {
        active = isActive;
    }

    public static void clear() {
        neededItemIds.clear();
    }

    public static void addNeededItem(String itemId) {
        neededItemIds.add(itemId);
        // Map fallbacks automatically
        if (itemId.equals("minecraft:white_dye")) {
            neededItemIds.add("minecraft:bone_meal");
        } else if (itemId.equals("minecraft:bone_meal")) {
            neededItemIds.add("minecraft:white_dye");
        } else if (itemId.equals("minecraft:coal")) {
            neededItemIds.add("minecraft:charcoal");
        } else if (itemId.equals("minecraft:charcoal")) {
            neededItemIds.add("minecraft:coal");
        }
    }

    public static boolean isNeeded(String itemId) {
        return neededItemIds.contains(itemId);
    }
}
