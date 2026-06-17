package com.example.autopalette.client.painter;

import com.example.autopalette.client.palette.ArtMapPalette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;

public class AutoPainter {
    public static final AutoPainter INSTANCE = new AutoPainter();

    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean active = false;
    private int delayTicks = 2; // configurable speed
    private boolean smoothRotation = true;
    private float maxRotationSpeed = 30.0f; // degrees per tick
    private String paintOrder = "Color-Optimized";

    // Queue of low-level draw steps
    private final Queue<ActionStep> stepQueue = new LinkedList<>();
    private int tickCounter = 0;
    
    // Status metrics
    private int totalPixelsToDraw = 0;
    private int drawnPixelsCount = 0;
    private String statusMessage = "Idle";
    private String missingItemName = null;

    // The image pixel target colors
    private ArtMapPalette.MappedColor[][] targetColors = new ArtMapPalette.MappedColor[128][128];
    private ItemFrameEntity activeCanvas = null;

    private final ArtMapPalette palette = new ArtMapPalette();

    private static float[][][] eastRotations = null;
    private static float[][][] westRotations = null;
    private static float[][][] northRotations = null;
    private static float[][][] southRotations = null;

    public static class PixelDrawAction {
        public final int x;
        public final int y;
        public final ArtMapPalette.MappedColor color;

        public PixelDrawAction(int x, int y, ArtMapPalette.MappedColor color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    public static class ActionStep {
        public enum Type {
            EQUIP_ITEM,
            ROTATE_TO,
            CLICK_CANVAS,
            WAIT_TICKS
        }

        public final Type type;
        public final String itemId;
        public final double targetYaw;
        public final double targetPitch;
        public final int ticks;

        public ActionStep(Type type, String itemId, double targetYaw, double targetPitch, int ticks) {
            this.type = type;
            this.itemId = itemId;
            this.targetYaw = targetYaw;
            this.targetPitch = targetPitch;
            this.ticks = ticks;
        }

        public static ActionStep equip(String itemId) {
            return new ActionStep(Type.EQUIP_ITEM, itemId, 0, 0, 0);
        }

        public static ActionStep rotate(double yaw, double pitch) {
            return new ActionStep(Type.ROTATE_TO, null, yaw, pitch, 0);
        }

        public static ActionStep click() {
            return new ActionStep(Type.CLICK_CANVAS, null, 0, 0, 0);
        }

        public static ActionStep waitTicks(int ticks) {
            return new ActionStep(Type.WAIT_TICKS, null, 0, 0, ticks);
        }
    }

    private AutoPainter() {}

    public boolean isActive() {
        return active;
    }

    public void startPainting(ArtMapPalette.MappedColor[][] colors, int delay, boolean smooth, float rotSpeed, String order) {
        if (client.player == null || client.world == null) return;
        
        activeCanvas = findEaselCanvas();
        if (activeCanvas == null) {
            client.player.sendMessage(Text.literal("§cCould not find ArtMap canvas! Please sit on the easel."), false);
            return;
        }

        this.targetColors = colors;
        this.delayTicks = delay;
        this.smoothRotation = smooth;
        this.maxRotationSpeed = rotSpeed;
        this.paintOrder = order;
        this.stepQueue.clear();
        this.tickCounter = 0;
        this.missingItemName = null;

        // Build list of pixels that need to be painted
        List<PixelDrawAction> actions = buildPaintActions();
        if (actions.isEmpty()) {
            client.player.sendMessage(Text.literal("§aDrawing completed! No changes needed."), false);
            return;
        }

        totalPixelsToDraw = actions.size();
        drawnPixelsCount = 0;
        statusMessage = "Queuing actions...";

        // Build steps from actions
        buildStepQueue(actions);

        active = true;
        statusMessage = "Painting...";
        client.player.sendMessage(Text.literal("§aStarting ArtMap Auto-Draw (" + totalPixelsToDraw + " pixels)..."), false);
    }

    public void stopPainting() {
        active = false;
        stepQueue.clear();
        statusMessage = "Stopped";
    }

    public int getProgressPercent() {
        if (totalPixelsToDraw == 0) return 0;
        return (drawnPixelsCount * 100) / totalPixelsToDraw;
    }

    public String getStatusText() {
        if (missingItemName != null) {
            return "§cMissing: " + missingItemName;
        }
        return active ? "Painting: " + drawnPixelsCount + " / " + totalPixelsToDraw + " (" + getProgressPercent() + "%)" : "Idle";
    }

    public void clientTick() {
        if (!active) return;

        if (client.player == null || client.world == null || activeCanvas == null || !activeCanvas.isAlive()) {
            stopPainting();
            return;
        }

        if (tickCounter > 0) {
            tickCounter--;
            return;
        }

        if (stepQueue.isEmpty()) {
            stopPainting();
            client.player.sendMessage(Text.literal("§aDrawing successfully finished!"), false);
            return;
        }

        // Process queue until a delay or rotation is encountered
        while (!stepQueue.isEmpty()) {
            ActionStep step = stepQueue.peek();

            if (step.type == ActionStep.Type.WAIT_TICKS) {
                tickCounter = step.ticks;
                stepQueue.poll();
                return;
            }

            if (step.type == ActionStep.Type.EQUIP_ITEM) {
                if (!tryEquipItem(step.itemId)) {
                    // Running out of dye
                    missingItemName = getDisplayNameFor(step.itemId);
                    client.player.sendMessage(Text.literal("§cStopping: Missing " + missingItemName + "!"), false);
                    stopPainting();
                    return;
                }
                stepQueue.poll();
                // Add a 1 tick delay after item equipping to ensure server registration
                tickCounter = 1;
                return;
            }

            if (step.type == ActionStep.Type.ROTATE_TO) {
                if (smoothRotation) {
                    if (!smoothRotateTowards(step.targetYaw, step.targetPitch)) {
                        // Not facing the target yet, tick again to continue rotating
                        return;
                    }
                } else {
                    client.player.setYaw((float) step.targetYaw);
                    client.player.setPitch((float) step.targetPitch);
                    client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                            (float) step.targetYaw, (float) step.targetPitch, client.player.isOnGround(), client.player.horizontalCollision
                    ));
                }
                stepQueue.poll();
                continue;
            }

            if (step.type == ActionStep.Type.CLICK_CANVAS) {
                // Click canvas
                client.interactionManager.attackEntity(client.player, activeCanvas);
                drawnPixelsCount++;
                stepQueue.poll();
                tickCounter = Math.max(0, delayTicks);
                return;
            }
        }
    }

    private boolean tryEquipItem(String itemId) {
        if (client.player == null || client.interactionManager == null) return false;

        int invSlot = findItemInInventory(itemId);
        if (invSlot == -1) return false;

        int currentSlot = client.player.getInventory().selectedSlot;

        // If it's already in the hotbar (0-8)
        if (invSlot >= 0 && invSlot <= 8) {
            if (currentSlot != invSlot) {
                client.player.getInventory().selectedSlot = invSlot;
                client.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(invSlot));
            }
            return true;
        }

        // If it is in the main inventory, swap it with slot 0
        int targetHotbarSlot = 0;
        int screenHandlerSlot = invSlot; // 9-35 match direct in PlayerScreenHandler

        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                screenHandlerSlot,
                targetHotbarSlot,
                SlotActionType.SWAP,
                client.player
        );

        // Update selected slot to the swap target
        if (currentSlot != targetHotbarSlot) {
            client.player.getInventory().selectedSlot = targetHotbarSlot;
            client.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(targetHotbarSlot));
        }

        return true;
    }

    private int findItemInInventory(String itemId) {
        if (client.player == null) return -1;
        Identifier targetId = Identifier.of(itemId);
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                Identifier itemKey = Registries.ITEM.getId(stack.getItem());
                if (itemKey.equals(targetId)) {
                    return i;
                }
            }
        }
        // Fallback for Coal and Charcoal interchangeability
        if (itemId.equals("minecraft:coal") || itemId.equals("minecraft:charcoal")) {
            Identifier fallbackId = Identifier.of(itemId.equals("minecraft:coal") ? "minecraft:charcoal" : "minecraft:coal");
            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    Identifier itemKey = Registries.ITEM.getId(stack.getItem());
                    if (itemKey.equals(fallbackId)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private String getDisplayNameFor(String itemId) {
        Identifier targetId = Identifier.of(itemId);
        return Registries.ITEM.get(targetId).getName().getString();
    }

    private boolean smoothRotateTowards(double targetYaw, double targetPitch) {
        if (client.player == null) return true;

        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();

        double yawDiff = MathHelper.wrapDegrees(targetYaw - yaw);
        double pitchDiff = targetPitch - pitch;

        boolean yawAligned = Math.abs(yawDiff) < 1.0;
        boolean pitchAligned = Math.abs(pitchDiff) < 1.0;

        if (yawAligned && pitchAligned) {
            client.player.setYaw((float) targetYaw);
            client.player.setPitch((float) targetPitch);
            client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    (float) targetYaw, (float) targetPitch, client.player.isOnGround(), client.player.horizontalCollision
            ));
            return true;
        }

        // Interpolate rotation
        float stepYaw = (float) (yaw + Math.signum(yawDiff) * Math.min(maxRotationSpeed, Math.abs(yawDiff)));
        float stepPitch = (float) (pitch + Math.signum(pitchDiff) * Math.min(maxRotationSpeed, Math.abs(pitchDiff)));

        client.player.setYaw(stepYaw);
        client.player.setPitch(stepPitch);
        client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                stepYaw, stepPitch, client.player.isOnGround(), client.player.horizontalCollision
        ));
        return false;
    }

    private ItemFrameEntity findEaselCanvas() {
        if (client.world == null || client.player == null) return null;
        List<ItemFrameEntity> frames = client.world.getEntitiesByClass(
                ItemFrameEntity.class,
                client.player.getBoundingBox().expand(3.0),
                entity -> entity.getHeldItemStack().getItem() == Items.FILLED_MAP
        );
        if (frames.isEmpty()) return null;
        
        ItemFrameEntity closest = null;
        double minDist = Double.MAX_VALUE;
        for (ItemFrameEntity frame : frames) {
            double dist = frame.squaredDistanceTo(client.player);
            if (dist < minDist) {
                minDist = dist;
                closest = frame;
            }
        }
        return closest;
    }

    private int getMapColorRgb(byte colorByte) {
        ArtMapPalette.MappedColor mc = palette.getByByte(colorByte);
        if (mc == null) return 0;
        return (mc.r << 16) | (mc.g << 8) | mc.b;
    }

    private List<PixelDrawAction> buildPaintActions() {
        List<PixelDrawAction> actions = new ArrayList<>();
        if (activeCanvas == null) return actions;

        // Try to get map state for the canvas item
        ItemStack mapStack = activeCanvas.getHeldItemStack();
        MapIdComponent mapId = mapStack.get(DataComponentTypes.MAP_ID);
        byte[] currentColors = null;
        if (mapId != null && client.world != null) {
            MapState mapState = client.world.getMapState(mapId);
            if (mapState != null) {
                currentColors = mapState.colors;
            }
        }

        // Default: iterate all 128x128 pixels
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                ArtMapPalette.MappedColor color = targetColors[x][y];
                if (color != null) {
                    if (currentColors != null) {
                        int pixelIndex = x + y * 128;
                        if (pixelIndex >= 0 && pixelIndex < currentColors.length) {
                            byte currentByte = currentColors[pixelIndex];
                            int currentRgb = getMapColorRgb(currentByte);

                            int curR = (currentRgb >> 16) & 0xFF;
                            int curG = (currentRgb >> 8) & 0xFF;
                            int curB = currentRgb & 0xFF;

                            int rDiff = curR - color.r;
                            int gDiff = curG - color.g;
                            int bDiff = curB - color.b;
                            double dist = Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);

                            if (dist < 8.0) {
                                continue; // Skip painting this pixel as it already matches close enough!
                            }
                        }
                    }
                    actions.add(new PixelDrawAction(x, y, color));
                }
            }
        }

        // Apply selected order
        sortActions(actions);
        return actions;
    }

    private void sortActions(List<PixelDrawAction> actions) {
        if ("Row by Row (L to R)".equalsIgnoreCase(paintOrder)) {
            actions.sort(Comparator.comparingInt((PixelDrawAction a) -> a.y)
                    .thenComparingInt(a -> a.x));
        } else if ("Row by Row (R to L)".equalsIgnoreCase(paintOrder)) {
            actions.sort(Comparator.comparingInt((PixelDrawAction a) -> a.y)
                    .thenComparing((PixelDrawAction a, PixelDrawAction b) -> Integer.compare(b.x, a.x)));
        } else if ("Column by Column".equalsIgnoreCase(paintOrder)) {
            actions.sort(Comparator.comparingInt((PixelDrawAction a) -> a.x)
                    .thenComparingInt(a -> a.y));
        } else if ("Snake".equalsIgnoreCase(paintOrder)) {
            actions.sort((a, b) -> {
                if (a.y != b.y) {
                    return Integer.compare(a.y, b.y);
                }
                return (a.y % 2 == 0) ? Integer.compare(a.x, b.x) : Integer.compare(b.x, a.x);
            });
        } else if ("Color-Optimized".equalsIgnoreCase(paintOrder)) {
            // Sort by base item to minimize hotbar swaps, then sort by proximity
            actions.sort(Comparator.comparing((PixelDrawAction a) -> a.color.baseEntry.itemId)
                    .thenComparingInt(a -> a.y)
                    .thenComparingInt(a -> a.x));
        }
    }

    private synchronized void loadRotationsIfNeeded() {
        if (eastRotations != null) return;
        
        eastRotations = loadRotationAsset("/assets/autopalette/east.ser");
        westRotations = loadRotationAsset("/assets/autopalette/west.ser");
        northRotations = loadRotationAsset("/assets/autopalette/north.ser");
        southRotations = loadRotationAsset("/assets/autopalette/south.ser");
    }

    @SuppressWarnings("unchecked")
    private float[][][] loadRotationAsset(String resourcePath) {
        float[][][] rotations = new float[128][128][2];
        try (InputStream is = AutoPainter.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[AutoPalette] Rotation asset not found: " + resourcePath);
                return rotations;
            }
            try (ObjectInputStream ois = new ObjectInputStream(is)) {
                Map<ArrayList<Integer>, ArrayList<Float>> map = (Map<ArrayList<Integer>, ArrayList<Float>>) ois.readObject();
                for (Map.Entry<ArrayList<Integer>, ArrayList<Float>> entry : map.entrySet()) {
                    int x = entry.getKey().get(0);
                    int y = entry.getKey().get(1);
                    float yaw = entry.getValue().get(0);
                    float pitch = entry.getValue().get(1);
                    if (x >= 0 && x < 128 && y >= 0 && y < 128) {
                        rotations[x][y][0] = yaw;
                        rotations[x][y][1] = pitch;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotations;
    }

    private void buildStepQueue(List<PixelDrawAction> actions) {
        stepQueue.clear();
        if (activeCanvas == null) return;

        loadRotationsIfNeeded();
        
        // Choose rotations cache based on client player's facing direction
        float[][][] rotationsCache;
        Direction facing = client.player != null ? client.player.getHorizontalFacing() : Direction.NORTH;
        if (facing == Direction.EAST) {
            rotationsCache = eastRotations;
        } else if (facing == Direction.WEST) {
            rotationsCache = westRotations;
        } else if (facing == Direction.NORTH) {
            rotationsCache = northRotations;
        } else if (facing == Direction.SOUTH) {
            rotationsCache = southRotations;
        } else {
            rotationsCache = northRotations; // fallback
        }

        String lastItem = null;

        for (PixelDrawAction act : actions) {
            float yaw = rotationsCache[act.x][act.y][0];
            float pitch = rotationsCache[act.x][act.y][1];

            // Step 1: Rotate to the pixel
            stepQueue.add(ActionStep.rotate(yaw, pitch));

            // Step 2: Equip base dye (if not already held)
            String baseDye = act.color.baseEntry.itemId;
            if (!baseDye.equals(lastItem)) {
                stepQueue.add(ActionStep.equip(baseDye));
                lastItem = baseDye;
            }

            // Step 3: Paint base color
            stepQueue.add(ActionStep.click());

            // Step 4: Handle shading modifications (lighten with feather, darken with coal)
            if (act.color.shade == ArtMapPalette.ShadeLevel.LIGHTENED) {
                stepQueue.add(ActionStep.equip("minecraft:feather"));
                lastItem = "minecraft:feather";
                stepQueue.add(ActionStep.click());
            } else if (act.color.shade == ArtMapPalette.ShadeLevel.DARKENED_1) {
                stepQueue.add(ActionStep.equip("minecraft:coal"));
                lastItem = "minecraft:coal";
                stepQueue.add(ActionStep.click());
            } else if (act.color.shade == ArtMapPalette.ShadeLevel.DARKENED_2) {
                stepQueue.add(ActionStep.equip("minecraft:coal"));
                lastItem = "minecraft:coal";
                stepQueue.add(ActionStep.click());
                stepQueue.add(ActionStep.click()); // click twice for level 2 darkness
            }
        }
    }
}
