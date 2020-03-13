package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class RangedProgressOverlay {
    public static RangedProgressOverlay instance;

    private final Minecraft mc;

    private GuiRangedProgress gui;

    public RangedProgressOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new GuiRangedProgress(mc);

        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        ItemStack activeStack = mc.player.getActiveItemStack();

        if (activeStack.getItem() instanceof ModularBowItem) {
            ModularBowItem item = (ModularBowItem) activeStack.getItem();
            gui.setProgress(
                    item.getProgress(activeStack, mc.player),
                    item.getOverbowCap(activeStack),
                    item.getOverbowRate(activeStack));
        } else {
            gui.setProgress(0, 0, 0);
        }

        gui.draw();
    }
}
