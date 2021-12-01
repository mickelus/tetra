package se.mickelus.tetra.items.modular.impl.crossbow;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.bow.GuiRangedProgress;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

public class CrossbowOverlay {
    public static CrossbowOverlay instance;

    private final Minecraft mc;

    private GuiRangedProgress gui;

    public CrossbowOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new GuiRangedProgress(mc);

        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        ItemStack activeStack = mc.player.getUseItem();

        if (activeStack.getItem() instanceof ModularCrossbowItem) {
            ModularCrossbowItem item = (ModularCrossbowItem) activeStack.getItem();
            gui.setProgress(item.getProgress(activeStack, mc.player), 0);
        } else {
            gui.setProgress(0, 0);
        }

        gui.draw();
    }
}
