package se.mickelus.tetra.blocks.multischematic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.TetraMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockSchematicScrollHandler {

    private static final Map<String, List<StackedMultiblockSchematicItem>> schematics = new HashMap<>();
    private static double scrollDelta;

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.isCreative() && Screen.hasAltDown()
                && player.getMainHandItem().getItem() instanceof StackedMultiblockSchematicItem) {
            double delta = event.getScrollDeltaY();
            scrollDelta = Math.signum(scrollDelta) == Math.signum(delta)
                    ? scrollDelta + delta
                    : delta;
            if (Math.abs(scrollDelta) > 1) {
                TetraMod.packetHandler.sendToServer(new MultiblockSchematicScrollPacket(scrollDelta > 0));
                scrollDelta = 0;
            }
            event.setCanceled(true);
        }
    }

    public static void shiftSchematic(Player player, boolean isIncrease) {
        if (player.isCreative()) {
            CastOptional.cast(player.getMainHandItem().getItem(), StackedMultiblockSchematicItem.class)
                    .ifPresent(item -> {
                        List<StackedMultiblockSchematicItem> parts = schematics.get(item.schematicBlock.schematic);
                        int index = parts.indexOf(item) + (isIncrease ? 1 : -1);
                        if (index >= 0 && index < parts.size()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(parts.get(index)));
                        }
                    });
        }
    }

    public static void setupSchematic(String identifier, int partCount) {
        ArrayList<StackedMultiblockSchematicItem> list = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            list.add(i, null);
        }
        schematics.put(identifier, list);
    }

    public static void addSchematic(String identifier, int index, StackedMultiblockSchematicItem item) {
        schematics.get(identifier).set(index, item);
    }
}
