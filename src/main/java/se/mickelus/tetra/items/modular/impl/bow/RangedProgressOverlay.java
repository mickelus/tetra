package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RangedProgressOverlay implements LayeredDraw.Layer {
    private final Minecraft mc;
    private final GuiRangedProgress gui;

    public RangedProgressOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new GuiRangedProgress(mc);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (mc.player != null) {
            ItemStack activeStack = mc.player.getUseItem();

            if (activeStack.getItem() instanceof ModularBowItem) {
                ModularBowItem item = (ModularBowItem) activeStack.getItem();
                gui.setProgress(
                        item.getProgress(activeStack, mc.player),
                        item.getOverbowProgress(activeStack, mc.player));
            } else {
                gui.setProgress(0, 0);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        this.gui.draw(graphics);
    }
}
