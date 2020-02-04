package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.util.CastOptional;

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
        gui.setProgress(CastOptional.cast(activeStack.getItem(), ModularBowItem.class)
            .map(item -> mc.player.getItemInUseCount() > 0
                    ? (item.getUseDuration(activeStack) - mc.player.getItemInUseCount()) * 1f / item.getDrawDuration(activeStack) : 0)
                .orElse(0f));

        gui.draw();
    }
}
