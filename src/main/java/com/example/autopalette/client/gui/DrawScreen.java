package com.example.autopalette.client.gui;

import com.example.autopalette.client.painter.AutoPainter;
import com.example.autopalette.client.palette.ArtMapPalette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DrawScreen extends Screen {
    private final ArtMapPalette palette = new ArtMapPalette();
    private final List<File> imageFiles = new ArrayList<>();
    
    private int selectedImageIndex = -1;
    private int scrollOffset = 0;
    
    // Config values
    private int drawDelay = 2; // Ticks
    private boolean smoothCam = true;
    private float rotationSpeed = 30.0f;
    private int selectedOrderIndex = 4; // default to Color-Optimized
    private final String[] paintOrders = {
            "Row by Row (L to R)",
            "Row by Row (R to L)",
            "Column by Column",
            "Snake",
            "Color-Optimized"
    };
    private int ditheringModeIndex = 1; // 0 = None, 1 = Floyd-Steinberg, 2 = Bayer 4x4, 3 = Bayer 8x8
    private int maxColorsIndex = 0;
    private final int[] colorLimits = { -1, 32, 16, 8 };
    private final int previewSize = 128;

    private static final int[][] BAYER_4X4 = {
        {  0,  8,  2, 10 },
        { 12,  4, 14,  6 },
        {  3, 11,  1,  9 },
        { 15,  7, 13,  5 }
    };

    private static final int[][] BAYER_8X8 = {
        {  0, 48, 12, 60,  3, 51, 15, 63 },
        { 32, 16, 44, 28, 35, 19, 47, 31 },
        {  8, 56,  4, 52, 11, 59,  7, 55 },
        { 40, 24, 36, 20, 43, 27, 39, 23 },
        {  2, 50, 14, 62,  1, 49, 13, 61 },
        { 34, 18, 46, 30, 33, 17, 45, 29 },
        { 10, 58,  6, 54,  9, 57,  5, 53 },
        { 42, 26, 38, 22, 41, 25, 37, 21 }
    };

    // Preview texture variables
    private NativeImageBackedTexture previewTexture = null;
    private Identifier previewTextureId = null;
    private ArtMapPalette.MappedColor[][] mappedColorsGrid = null;

    // Controls
    private ButtonWidget drawButton;
    private ButtonWidget orderButton;
    private ButtonWidget ditheringButton;
    private ButtonWidget smoothCamButton;
    private ButtonWidget delayMinusButton;
    private ButtonWidget delayPlusButton;
    private ButtonWidget settingsTabButton;
    private ButtonWidget materialsTabButton;
    private ButtonWidget matScrollUpButton;
    private ButtonWidget matScrollDownButton;
    private ButtonWidget maxColorsButton;

    // Tab state
    private boolean showMaterialsTab = false;
    private int materialScrollOffset = 0;
    private final List<MaterialEntry> materialsList = new ArrayList<>();

    public DrawScreen() {
        super(Text.literal("AutoPalette Panel v1.1.0"));
    }

    @Override
    protected void init() {
        refreshImageList();

        // 1. Scroll Up Button
        addDrawableChild(ButtonWidget.builder(Text.literal("▲"), button -> {
            if (scrollOffset > 0) scrollOffset--;
        }).dimensions(10, 35, 120, 15).build());

        // 2. Scroll Down Button
        addDrawableChild(ButtonWidget.builder(Text.literal("▼"), button -> {
            if (scrollOffset < Math.max(0, imageFiles.size() - 5)) scrollOffset++;
        }).dimensions(10, 145, 120, 15).build());

        // 3. Selection Buttons (Image List items)
        for (int i = 0; i < 5; i++) {
            final int index = i;
            addDrawableChild(ButtonWidget.builder(Text.literal("Empty"), button -> {
                int targetIndex = scrollOffset + index;
                if (targetIndex < imageFiles.size()) {
                    selectedImageIndex = targetIndex;
                    loadSelectedImagePreview();
                }
            }).dimensions(10, 52 + i * 18, 120, 16).build());
        }

        // 4. Tab Selector Buttons
        settingsTabButton = addDrawableChild(ButtonWidget.builder(Text.literal("Settings"), button -> {
            showMaterialsTab = false;
            updateTabVisibility();
        }).dimensions(145, 35, 72, 20).build());

        materialsTabButton = addDrawableChild(ButtonWidget.builder(Text.literal("Materials"), button -> {
            showMaterialsTab = true;
            updateTabVisibility();
        }).dimensions(220, 35, 75, 20).build());

        // 5. Config Buttons (Settings Tab)
        // Delay -
        delayMinusButton = addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> {
            if (drawDelay > 0) drawDelay--;
        }).dimensions(145, 80, 20, 20).build());

        // Delay +
        delayPlusButton = addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            if (drawDelay < 20) drawDelay++;
        }).dimensions(225, 80, 20, 20).build());

        // Dithering Button
        ditheringButton = addDrawableChild(ButtonWidget.builder(Text.literal("Dithering: Floyd-Steinberg"), button -> {
            ditheringModeIndex = (ditheringModeIndex + 1) % 4;
            updateButtonsText();
            loadSelectedImagePreview();
        }).dimensions(145, 105, 150, 20).build());

        // Smooth Camera Button
        smoothCamButton = addDrawableChild(ButtonWidget.builder(Text.literal("Smooth Cam: ON"), button -> {
            smoothCam = !smoothCam;
            updateButtonsText();
        }).dimensions(145, 130, 150, 20).build());

        // Draw Order Button
        orderButton = addDrawableChild(ButtonWidget.builder(Text.literal("Order: Color-Optimized"), button -> {
            selectedOrderIndex = (selectedOrderIndex + 1) % paintOrders.length;
            updateButtonsText();
        }).dimensions(145, 155, 150, 20).build());

        // Max Colors Button
        maxColorsButton = addDrawableChild(ButtonWidget.builder(Text.literal("Max Colors: Unlimited"), button -> {
            maxColorsIndex = (maxColorsIndex + 1) % colorLimits.length;
            updateButtonsText();
            loadSelectedImagePreview();
            materialScrollOffset = 0;
        }).dimensions(145, 180, 150, 20).build());

        // 6. Materials Tab Scroll Buttons
        matScrollUpButton = addDrawableChild(ButtonWidget.builder(Text.literal("▲"), button -> {
            if (materialScrollOffset > 0) materialScrollOffset--;
        }).dimensions(280, 75, 15, 20).build());

        // 7. Materials Tab Scroll Down Button
        matScrollDownButton = addDrawableChild(ButtonWidget.builder(Text.literal("▼"), button -> {
            if (materialScrollOffset < materialsList.size() - 8) materialScrollOffset++;
        }).dimensions(280, 100, 15, 20).build());

        // 8. Start / Stop Draw Button
        drawButton = addDrawableChild(ButtonWidget.builder(Text.literal("START DRAWING"), button -> {
            if (AutoPainter.INSTANCE.isActive()) {
                AutoPainter.INSTANCE.stopPainting();
            } else if (mappedColorsGrid != null) {
                AutoPainter.INSTANCE.startPainting(
                        mappedColorsGrid,
                        drawDelay,
                        smoothCam,
                        rotationSpeed,
                        paintOrders[selectedOrderIndex]
                );
                this.close();
            }
        }).dimensions(145, 205, 150, 20).build());

        updateButtonsText();
        updateListButtonLabels();
    }

    private void refreshImageList() {
        imageFiles.clear();
        File folder = new File(MinecraftClient.getInstance().runDirectory, "config/autopalette/images");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
                    imageFiles.add(file);
                }
            }
        }
    }

    private void updateButtonsText() {
        String ditheringText = switch (ditheringModeIndex) {
            case 0 -> "None";
            case 1 -> "Floyd-Steinberg";
            case 2 -> "Bayer 4x4";
            case 3 -> "Bayer 8x8";
            default -> "None";
        };
        ditheringButton.setMessage(Text.literal("Dithering: " + ditheringText));
        smoothCamButton.setMessage(Text.literal("Smooth Cam: " + (smoothCam ? "ON" : "OFF")));
        orderButton.setMessage(Text.literal("Order: " + getShortOrderName(paintOrders[selectedOrderIndex])));
        int limit = colorLimits[maxColorsIndex];
        maxColorsButton.setMessage(Text.literal("Max Colors: " + (limit == -1 ? "Unlimited" : limit)));
        
        if (AutoPainter.INSTANCE.isActive()) {
            drawButton.setMessage(Text.literal("STOP DRAWING"));
        } else {
            drawButton.setMessage(Text.literal("START DRAWING"));
        }
    }

    private String getShortOrderName(String fullName) {
        if (fullName.contains("Row")) return "Row by Row";
        if (fullName.contains("Column")) return "Col by Col";
        return fullName;
    }

    private void updateListButtonLabels() {
        int listBtnCount = 0;
        for (var child : this.children()) {
            if (child instanceof ButtonWidget btn) {
                if (btn.getX() == 10 && btn.getY() >= 52 && btn.getY() <= 124) {
                    int targetIndex = scrollOffset + listBtnCount;
                    if (targetIndex < imageFiles.size()) {
                        String name = imageFiles.get(targetIndex).getName();
                        if (name.length() > 14) {
                            name = name.substring(0, 11) + "...";
                        }
                        btn.setMessage(Text.literal(name));
                        btn.active = true;
                    } else {
                        btn.setMessage(Text.literal("- Empty -"));
                        btn.active = false;
                    }
                    listBtnCount++;
                }
            }
        }
    }

    private ArtMapPalette.MappedColor getClosestColorFrom(int r, int g, int b, Set<ArtMapPalette.PaletteEntry> allowedEntries) {
        ArtMapPalette.MappedColor closest = null;
        double minDistance = Double.MAX_VALUE;

        for (ArtMapPalette.MappedColor color : palette.getMappedColors()) {
            if (allowedEntries.contains(color.baseEntry)) {
                double dist = getColorDistance(r, g, b, color.r, color.g, color.b);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = color;
                }
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

    private void loadSelectedImagePreview() {
        if (selectedImageIndex < 0 || selectedImageIndex >= imageFiles.size()) return;
        File file = imageFiles.get(selectedImageIndex);

        try {
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) return;

            // Resize to 128x128
            BufferedImage resized = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resized.createGraphics();
            graphics.drawImage(originalImage, 0, 0, 128, 128, null);
            graphics.dispose();

            // Perform color mapping
            mappedColorsGrid = new ArtMapPalette.MappedColor[128][128];
            NativeImage previewImg = new NativeImage(128, 128, false);

            // Determine allowed base entries based on selected max colors limit
            int limit = colorLimits[maxColorsIndex];
            Set<ArtMapPalette.PaletteEntry> allowedBaseEntries = new HashSet<>();
            if (limit == -1) {
                allowedBaseEntries.addAll(palette.getBaseEntries());
            } else {
                Map<ArtMapPalette.PaletteEntry, Integer> frequencyMap = new HashMap<>();
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        int rgb = resized.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        ArtMapPalette.MappedColor closest = palette.getClosestColor(r, g, b);
                        if (closest != null && closest.baseEntry != null) {
                            frequencyMap.put(closest.baseEntry, frequencyMap.getOrDefault(closest.baseEntry, 0) + 1);
                        }
                    }
                }
                List<Map.Entry<ArtMapPalette.PaletteEntry, Integer>> sortedEntries = new ArrayList<>(frequencyMap.entrySet());
                sortedEntries.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));
                for (int i = 0; i < Math.min(limit, sortedEntries.size()); i++) {
                    allowedBaseEntries.add(sortedEntries.get(i).getKey());
                }
                if (allowedBaseEntries.isEmpty() && !palette.getBaseEntries().isEmpty()) {
                    allowedBaseEntries.add(palette.getBaseEntries().get(0));
                }
            }

            if (ditheringModeIndex == 1) {
                // Floyd-Steinberg Dithering
                double[][][] rgbGrid = new double[128][128][3];
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        int rgb = resized.getRGB(x, y);
                        rgbGrid[x][y][0] = (rgb >> 16) & 0xFF; // R
                        rgbGrid[x][y][1] = (rgb >> 8) & 0xFF;  // G
                        rgbGrid[x][y][2] = rgb & 0xFF;         // B
                    }
                }

                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        double r = Math.min(255, Math.max(0, rgbGrid[x][y][0]));
                        double g = Math.min(255, Math.max(0, rgbGrid[x][y][1]));
                        double b = Math.min(255, Math.max(0, rgbGrid[x][y][2]));

                        ArtMapPalette.MappedColor closest = getClosestColorFrom((int) r, (int) g, (int) b, allowedBaseEntries);
                        mappedColorsGrid[x][y] = closest;

                        double errR = r - closest.r;
                        double errG = g - closest.g;
                        double errB = b - closest.b;

                        distributeError(rgbGrid, x + 1, y, errR, errG, errB, 7.0 / 16.0);
                        distributeError(rgbGrid, x - 1, y + 1, errR, errG, errB, 3.0 / 16.0);
                        distributeError(rgbGrid, x, y + 1, errR, errG, errB, 5.0 / 16.0);
                        distributeError(rgbGrid, x + 1, y + 1, errR, errG, errB, 1.0 / 16.0);

                        int abgr = 0xFF000000 | (closest.b << 16) | (closest.g << 8) | closest.r;
                        previewImg.setColor(x, y, abgr);
                    }
                }
            } else if (ditheringModeIndex == 2 || ditheringModeIndex == 3) {
                // Ordered Bayer Dithering (4x4 or 8x8)
                int size = (ditheringModeIndex == 2) ? 4 : 8;
                int[][] bayerMatrix = (ditheringModeIndex == 2) ? BAYER_4X4 : BAYER_8X8;
                double spread = 32.0;

                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        int rgb = resized.getRGB(x, y);
                        int origR = (rgb >> 16) & 0xFF;
                        int origG = (rgb >> 8) & 0xFF;
                        int origB = rgb & 0xFF;

                        double factor = (bayerMatrix[x % size][y % size] + 0.5) / (size * size) - 0.5;
                        int r = (int) (origR + factor * spread);
                        int g = (int) (origG + factor * spread);
                        int b = (int) (origB + factor * spread);

                        r = Math.min(255, Math.max(0, r));
                        g = Math.min(255, Math.max(0, g));
                        b = Math.min(255, Math.max(0, b));

                        ArtMapPalette.MappedColor closest = getClosestColorFrom(r, g, b, allowedBaseEntries);
                        mappedColorsGrid[x][y] = closest;

                        int abgr = 0xFF000000 | (closest.b << 16) | (closest.g << 8) | closest.r;
                        previewImg.setColor(x, y, abgr);
                    }
                }
            } else {
                // Direct Nearest Color mapping (None)
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        int rgb = resized.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        ArtMapPalette.MappedColor closest = getClosestColorFrom(r, g, b, allowedBaseEntries);
                        mappedColorsGrid[x][y] = closest;

                        int abgr = 0xFF000000 | (closest.b << 16) | (closest.g << 8) | closest.r;
                        previewImg.setColor(x, y, abgr);
                    }
                }
            }

            // Update registered texture
            if (previewTexture != null) {
                previewTexture.close();
            }
            previewTexture = new NativeImageBackedTexture(() -> "autopalette_preview", previewImg);
            previewTextureId = Identifier.of("autopalette", "preview");
            MinecraftClient.getInstance().getTextureManager().registerTexture(previewTextureId, previewTexture);
            calculateMaterialsList();
            materialScrollOffset = 0;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void distributeError(double[][][] rgbGrid, int x, int y, double errR, double errG, double errB, double factor) {
        if (x >= 0 && x < 128 && y >= 0 && y < 128) {
            rgbGrid[x][y][0] += errR * factor;
            rgbGrid[x][y][1] += errG * factor;
            rgbGrid[x][y][2] += errB * factor;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        updateListButtonLabels();

        // Title
        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, 8, 0xFFFFFFFF);

        // Subtitles
        context.drawTextWithShadow(textRenderer, Text.literal("Images Directory"), 10, 22, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, Text.literal("Settings Panel"), 145, 22, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, Text.literal("Preview (Mapped)"), 300, 22, 0xFFAAAAAA);

        // Render preview texture box
        int previewX = 300;
        int previewY = 35;
 
        context.drawBorder(previewX - 1, previewY - 1, previewSize + 2, previewSize + 2, 0xFFAAAAAA);
        if (previewTextureId != null) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, previewTextureId, previewX, previewY, 0.0f, 0.0f, previewSize, previewSize, 128, 128);
        } else {
            context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF333333);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No Preview"), previewX + previewSize / 2, previewY + previewSize / 2 - 4, 0xFF777777);
        }
 
        if (!showMaterialsTab) {
            // Selected Image Label
            if (selectedImageIndex >= 0 && selectedImageIndex < imageFiles.size()) {
                String selectedName = imageFiles.get(selectedImageIndex).getName();
                if (selectedName.length() > 22) {
                    selectedName = selectedName.substring(0, 19) + "...";
                }
                context.drawTextWithShadow(textRenderer, Text.literal("Selected: " + selectedName), 145, 58, 0xFFFFD700);
            } else {
                context.drawTextWithShadow(textRenderer, Text.literal("Selected: None"), 145, 58, 0xFFFF0000);
            }
 
            // Delay display value
            context.drawTextWithShadow(textRenderer, Text.literal("Delay:"), 145, 72, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(drawDelay + " Ticks"), 195, 85, 0xFF00FF00);
        } else {
            // Render Materials List
            context.drawTextWithShadow(textRenderer, Text.literal("Needed Materials:"), 145, 60, 0xFFFFAAAA);
            
            int startY = 75;
            int itemsCount = Math.min(8, materialsList.size() - materialScrollOffset);
            for (int i = 0; i < itemsCount; i++) {
                MaterialEntry entry = materialsList.get(materialScrollOffset + i);
                int y = startY + i * 18;
                
                // Render item icon
                if (entry.itemId != null && !entry.itemId.isEmpty()) {
                    net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(entry.itemId));
                    if (item != net.minecraft.item.Items.AIR) {
                        context.drawItem(new net.minecraft.item.ItemStack(item), 145, y - 3);
                    }
                }

                String name = entry.name;
                if (name.length() > 13) {
                    name = name.substring(0, 11) + "..";
                }
                
                int stacks = entry.count / 64;
                int remaining = entry.count % 64;
                String amtStr = entry.count + "";
                if (stacks > 0) {
                    amtStr = stacks + "s+" + remaining;
                }
                
                context.drawTextWithShadow(textRenderer, Text.literal(name), 165, y, entry.color);
                context.drawTextWithShadow(textRenderer, Text.literal("x" + amtStr), 252, y, 0xFFBBBBBB);
            }
            
            if (materialsList.isEmpty()) {
                context.drawTextWithShadow(textRenderer, Text.literal("No image selected"), 145, 90, 0xFF777777);
            }
        }

        // Painter Status
        int statusY = previewY + previewSize + 10;
        context.drawTextWithShadow(textRenderer, Text.literal("Status:"), 300, statusY, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, Text.literal(AutoPainter.INSTANCE.getStatusText()), 300, statusY + 13, 0xFFFFFFFF);
 
        // Progress bar if painting
        if (AutoPainter.INSTANCE.isActive()) {
            int progressWidth = 110;
            int progressX = 300;
            int progressY = statusY + 28;
            int progressVal = (int) (progressWidth * (AutoPainter.INSTANCE.getProgressPercent() / 100.0));
 
            context.drawBorder(progressX - 1, progressY - 1, progressWidth + 2, 8, 0xFFAAAAAA);
            context.fill(progressX, progressY, progressX + progressWidth, progressY + 6, 0xFF333333);
            context.fill(progressX, progressY, progressX + progressVal, progressY + 6, 0xFF00FF00);
        }
    }

    public static class MaterialEntry {
        public final String name;
        public final String itemId;
        public final int count;
        public final int color;

        public MaterialEntry(String name, String itemId, int count, int color) {
            this.name = name;
            this.itemId = itemId;
            this.count = count;
            this.color = color;
        }
    }

    private void calculateMaterialsList() {
        materialsList.clear();
        if (mappedColorsGrid == null) return;

        // Count dye counts for each pixel
        Map<String, Integer> dyeCounts = new HashMap<>();
        int feathersTotal = 0;
        int coalTotal = 0;

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                ArtMapPalette.MappedColor color = mappedColorsGrid[x][y];
                if (color != null) {
                    String name = color.baseEntry.displayName;
                    dyeCounts.put(name, dyeCounts.getOrDefault(name, 0) + 1);

                    if (color.shade == ArtMapPalette.ShadeLevel.LIGHTENED) {
                        feathersTotal += 1;
                    } else if (color.shade == ArtMapPalette.ShadeLevel.DARKENED_1) {
                        coalTotal += 1;
                    } else if (color.shade == ArtMapPalette.ShadeLevel.DARKENED_2) {
                        coalTotal += 2;
                    }
                }
            }
        }

        // Sort dyes by count descending
        List<Map.Entry<String, Integer>> sortedDyes = new ArrayList<>(dyeCounts.entrySet());
        sortedDyes.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Map dye names to their item IDs from the palette
        Map<String, String> nameToId = new HashMap<>();
        for (ArtMapPalette.PaletteEntry pe : palette.getBaseEntries()) {
            nameToId.put(pe.displayName, pe.itemId);
        }

        // Populate final list of materials
        for (Map.Entry<String, Integer> entry : sortedDyes) {
            String itemId = nameToId.getOrDefault(entry.getKey(), "");
            materialsList.add(new MaterialEntry(entry.getKey(), itemId, entry.getValue(), 0xFFFFFFFF));
        }

        if (feathersTotal > 0) {
            materialsList.add(new MaterialEntry("Feather", "minecraft:feather", feathersTotal, 0xFF55FFFF));
        }
        if (coalTotal > 0) {
            materialsList.add(new MaterialEntry("Coal/Charcoal", "minecraft:coal", coalTotal, 0xFFAAAAAA));
        }
    }

    private void updateTabVisibility() {
        boolean showSettings = !showMaterialsTab;
        
        ditheringButton.visible = showSettings;
        ditheringButton.active = showSettings;
        
        smoothCamButton.visible = showSettings;
        smoothCamButton.active = showSettings;
        
        orderButton.visible = showSettings;
        orderButton.active = showSettings;
        
        delayMinusButton.visible = showSettings;
        delayMinusButton.active = showSettings;
        
        delayPlusButton.visible = showSettings;
        delayPlusButton.active = showSettings;

        maxColorsButton.visible = showSettings;
        maxColorsButton.active = showSettings;

        boolean showMatScroll = showMaterialsTab && materialsList.size() > 8;
        matScrollUpButton.visible = showMatScroll;
        matScrollUpButton.active = showMatScroll;
        matScrollDownButton.visible = showMatScroll;
        matScrollDownButton.active = showMatScroll;

        settingsTabButton.active = showMaterialsTab;
        materialsTabButton.active = !showMaterialsTab;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (showMaterialsTab && mouseX >= 145 && mouseX <= 295 && mouseY >= 60 && mouseY <= 180) {
            if (verticalAmount > 0) {
                if (materialScrollOffset > 0) materialScrollOffset--;
            } else if (verticalAmount < 0) {
                if (materialScrollOffset < materialsList.size() - 8) materialScrollOffset++;
            }
            updateTabVisibility();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        super.close();
        if (previewTexture != null) {
            previewTexture.close();
            previewTexture = null;
            previewTextureId = null;
        }
    }
}
