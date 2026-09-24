package com.example.autopalette.mixin;

import com.example.autopalette.client.util.MaterialHighlighter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (MaterialHighlighter.isActive()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                String itemIdStr = itemId.toString();

                if (MaterialHighlighter.isNeeded(itemIdStr)) {
                    // Draw a translucent green highlight overlay (16x16)
                    // 0x5055FF55 is a clean, semi-transparent green color
                    context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x5055FF55);
                }
            }
        }
    }
}
