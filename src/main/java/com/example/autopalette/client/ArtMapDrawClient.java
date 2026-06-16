package com.example.autopalette.client;

import com.example.autopalette.client.gui.DrawScreen;
import com.example.autopalette.client.painter.AutoPainter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ArtMapDrawClient implements ClientModInitializer {
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        // Register keybinding to open drawing screen (default key: H)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autopalette.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.autopalette"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if key is pressed to open DrawScreen
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new DrawScreen());
                }
            }

            // Run the painter state machine logic each tick
            AutoPainter.INSTANCE.clientTick();
        });
    }
}
