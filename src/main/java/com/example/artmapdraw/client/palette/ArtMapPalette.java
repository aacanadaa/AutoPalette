package com.example.artmapdraw.client.palette;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtMapPalette {
    private static final String MAPPINGS_RESOURCE_PATH = "/assets/artmapdraw/mappings.json";

    public static class PaletteEntry {
        public String itemId;
        public String displayName;
        public int colorId;
        public int r;
        public int g;
        public int b;

        public PaletteEntry(String itemId, String displayName, int colorId, int r, int g, int b) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.colorId = colorId;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public static class MappedColor {
        public PaletteEntry baseEntry;
        public ShadeLevel shade;
        public int r, g, b;

        public MappedColor(PaletteEntry baseEntry, ShadeLevel shade, int r, int g, int b) {
            this.baseEntry = baseEntry;
            this.shade = shade;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public enum ShadeLevel {
        DARKENED_1(0, "Darkened (1 Coal)", 0),
        BASE(1, "Base", 1),
        LIGHTENED(2, "Lightened (Feather)", 2),
        DARKENED_2(3, "Darkened (2 Coal)", 3);

        public final int id;
        public final String name;
        public final int jsonIndex;

        ShadeLevel(int id, String name, int jsonIndex) {
            this.id = id;
            this.name = name;
            this.jsonIndex = jsonIndex;
        }
    }

    private final List<PaletteEntry> baseEntries = new ArrayList<>();
    private final List<MappedColor> mappedColors = new ArrayList<>();
    private final Map<Integer, MappedColor> byteToColorMap = new HashMap<>();

    public ArtMapPalette() {
        loadPaletteFromResource();
    }

    public List<MappedColor> getMappedColors() {
        return mappedColors;
    }

    public List<PaletteEntry> getBaseEntries() {
        return baseEntries;
    }

    public MappedColor getByByte(byte colorByte) {
        return byteToColorMap.get((int) colorByte & 0xFF);
    }

    private void loadPaletteFromResource() {
        baseEntries.clear();
        mappedColors.clear();
        byteToColorMap.clear();

        // Statically map IDs to Minecraft items
        Map<Integer, String> itemMappings = new HashMap<>();
        itemMappings.put(0, "minecraft:ender_eye");
        itemMappings.put(1, "minecraft:short_grass");
        itemMappings.put(2, "minecraft:pumpkin_seeds");
        itemMappings.put(3, "minecraft:cobweb");
        itemMappings.put(4, "minecraft:red_dye");
        itemMappings.put(5, "minecraft:ice");
        itemMappings.put(6, "minecraft:light_gray_dye");
        itemMappings.put(7, "minecraft:oak_leaves");
        itemMappings.put(8, "minecraft:snow");
        itemMappings.put(9, "minecraft:gray_dye");
        itemMappings.put(10, "minecraft:melon_seeds");
        itemMappings.put(11, "minecraft:ghast_tear");
        itemMappings.put(12, "minecraft:lapis_block");
        itemMappings.put(13, "minecraft:dark_oak_log");
        itemMappings.put(14, "minecraft:bone_meal");
        itemMappings.put(15, "minecraft:orange_dye");
        itemMappings.put(16, "minecraft:magenta_dye");
        itemMappings.put(17, "minecraft:light_blue_dye");
        itemMappings.put(18, "minecraft:yellow_dye");
        itemMappings.put(19, "minecraft:lime_dye");
        itemMappings.put(20, "minecraft:pink_dye");
        itemMappings.put(21, "minecraft:flint");
        itemMappings.put(22, "minecraft:gunpowder");
        itemMappings.put(23, "minecraft:cyan_dye");
        itemMappings.put(24, "minecraft:purple_dye");
        itemMappings.put(25, "minecraft:lapis_lazuli");
        itemMappings.put(26, "minecraft:cocoa_beans");
        itemMappings.put(27, "minecraft:green_dye");
        itemMappings.put(28, "minecraft:brick");
        itemMappings.put(29, "minecraft:ink_sac");
        itemMappings.put(30, "minecraft:gold_nugget");
        itemMappings.put(31, "minecraft:prismarine_crystals");
        itemMappings.put(32, "minecraft:lapis_ore");
        itemMappings.put(33, "minecraft:emerald");
        itemMappings.put(34, "minecraft:birch_wood");
        itemMappings.put(35, "minecraft:nether_wart");
        itemMappings.put(36, "minecraft:egg");
        itemMappings.put(37, "minecraft:magma_cream");
        itemMappings.put(38, "minecraft:beetroot");
        itemMappings.put(39, "minecraft:mycelium");
        itemMappings.put(40, "minecraft:glowstone_dust");
        itemMappings.put(41, "minecraft:slime_ball");
        itemMappings.put(42, "minecraft:spider_eye");
        itemMappings.put(43, "minecraft:soul_sand");
        itemMappings.put(44, "minecraft:brown_mushroom");
        itemMappings.put(45, "minecraft:iron_nugget");
        itemMappings.put(46, "minecraft:chorus_fruit");
        itemMappings.put(47, "minecraft:purpur_block");
        itemMappings.put(48, "minecraft:podzol");
        itemMappings.put(49, "minecraft:poisonous_potato");
        itemMappings.put(50, "minecraft:apple");
        itemMappings.put(51, "minecraft:charcoal");
        itemMappings.put(52, "minecraft:crimson_nylium");
        itemMappings.put(53, "minecraft:crimson_stem");
        itemMappings.put(54, "minecraft:crimson_hyphae");
        itemMappings.put(55, "minecraft:warped_nylium");
        itemMappings.put(56, "minecraft:warped_stem");
        itemMappings.put(57, "minecraft:warped_hyphae");
        itemMappings.put(58, "minecraft:warped_wart_block");
        itemMappings.put(59, "minecraft:cobbled_deepslate");
        itemMappings.put(60, "minecraft:raw_iron");
        itemMappings.put(61, "minecraft:glow_lichen");

        try (InputStream is = ArtMapPalette.class.getResourceAsStream(MAPPINGS_RESOURCE_PATH)) {
            if (is == null) {
                System.err.println("[ArtMapDraw] mappings.json resource not found!");
                return;
            }
            try (InputStreamReader isr = new InputStreamReader(is)) {
                JsonObject rootObj = JsonParser.parseReader(isr).getAsJsonObject();
                JsonObject versionObj = rootObj.getAsJsonObject("1.20");

                for (String keyStr : versionObj.keySet()) {
                    int colorId = Integer.parseInt(keyStr);
                    JsonObject colorEntry = versionObj.getAsJsonObject(keyStr);
                    int baseRgb = colorEntry.get("base").getAsInt();
                    JsonArray colorsArr = colorEntry.getAsJsonArray("colors");

                    String itemId = itemMappings.get(colorId);
                    if (itemId == null) continue;

                    // Fetch display name from registry dynamically if client is initialized
                    String displayName = itemId;
                    try {
                        displayName = Registries.ITEM.get(Identifier.of(itemId)).getName().getString();
                    } catch (Exception ignored) {}

                    int baseR = (baseRgb >> 16) & 0xFF;
                    int baseG = (baseRgb >> 8) & 0xFF;
                    int baseB = baseRgb & 0xFF;

                    PaletteEntry paletteEntry = new PaletteEntry(itemId, displayName, colorId, baseR, baseG, baseB);
                    if (colorId != 0) {
                        baseEntries.add(paletteEntry);
                    }

                    // Process shade variant colors
                    for (int variant = 0; variant < colorsArr.size(); variant++) {
                        int variantRgb = colorsArr.get(variant).getAsInt();
                        int vr = (variantRgb >> 16) & 0xFF;
                        int vg = (variantRgb >> 8) & 0xFF;
                        int vb = variantRgb & 0xFF;

                        // Find corresponding ShadeLevel
                        ShadeLevel shade = null;
                        for (ShadeLevel sl : ShadeLevel.values()) {
                            if (sl.jsonIndex == variant) {
                                shade = sl;
                                break;
                            }
                        }
                        if (shade == null) continue;

                        MappedColor mappedColor = new MappedColor(paletteEntry, shade, vr, vg, vb);
                        if (colorId != 0) {
                            mappedColors.add(mappedColor);
                        }

                        // Register in byte-to-color lookup table
                        int byteVal = colorId * 4 + variant;
                        byteToColorMap.put(byteVal, mappedColor);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MappedColor getClosestColor(int r, int g, int b) {
        MappedColor closest = null;
        double minDistance = Double.MAX_VALUE;

        for (MappedColor color : mappedColors) {
            double dist = getColorDistance(r, g, b, color.r, color.g, color.b);
            if (dist < minDistance) {
                minDistance = dist;
                closest = color;
            }
        }
        return closest;
    }

    private double getColorDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        long rDiff = r1 - r2;
        long gDiff = g1 - g2;
        long bDiff = b1 - b2;
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }
}
