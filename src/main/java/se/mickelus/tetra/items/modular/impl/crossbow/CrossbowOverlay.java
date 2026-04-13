package se.mickelus.tetra.items.modular.impl.crossbow;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.bow.GuiRangedProgress;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CrossbowOverlay implements LayeredDraw.Layer {
    private final Minecraft mc;
    private final GuiRangedProgress gui;

    public CrossbowOverlay(Minecraft mc) {
        this.mc = mc;
        gui = new GuiRangedProgress(mc);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (mc.player != null) {
            ItemStack activeStack = mc.player.getUseItem();

            if (activeStack.getItem() instanceof ModularCrossbowItemImpl) {
                ModularCrossbowItemImpl item = (ModularCrossbowItemImpl) activeStack.getItem();
                this.gui.setProgress(item.getProgress(activeStack, mc.player), 0);
            } else {
                this.gui.setProgress(0, 0);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        this.gui.draw(graphics);
    }
}
