package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RangedProgressOverlay {
    public static RangedProgressOverlay instance;

    private final Minecraft mc;

    private final GuiRangedProgress gui;

    public RangedProgressOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new GuiRangedProgress(mc);

        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        ItemStack activeStack = mc.player.getUseItem();

        if (activeStack.getItem() instanceof ModularBowItem) {
            ModularBowItem item = (ModularBowItem) activeStack.getItem();
            gui.setProgress(
                    item.getProgress(activeStack, mc.player),
                    item.getOverbowProgress(activeStack, mc.player));
        } else {
            gui.setProgress(0, 0);
        }

        gui.draw();
    }
}
