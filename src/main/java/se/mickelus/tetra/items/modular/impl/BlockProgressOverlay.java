package se.mickelus.tetra.items.modular.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.bow.GuiRangedProgress;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.util.CastOptional;

public class BlockProgressOverlay {
    public static BlockProgressOverlay instance;

    private final Minecraft mc;

    private GuiBlockProgress gui;

    public BlockProgressOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new GuiBlockProgress(mc);

        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        ItemStack activeStack = mc.player.getActiveItemStack();

        gui.setProgress(
                CastOptional.cast(activeStack.getItem(), ItemModularHandheld.class)
                        .map(item -> item.getBlockProgress(activeStack, mc.player))
                        .orElse(0f));

        gui.draw();
    }
}
