package com.example.autopalette;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import java.io.File;

public class ArtMapDraw implements ModInitializer {
    public static final String MOD_ID = "autopalette";

    @Override
    public void onInitialize() {
        // Create necessary directory structure
        File configFolder = new File(MinecraftClient.getInstance().runDirectory, "config/autopalette");
        File imagesFolder = new File(configFolder, "images");

        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }
    }
}
