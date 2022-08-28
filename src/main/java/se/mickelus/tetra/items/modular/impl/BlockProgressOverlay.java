package se.mickelus.tetra.items.modular.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockProgressOverlay {
    public static BlockProgressOverlay instance;

    private final Minecraft mc;

    private final GuiBlockProgress gui;

    public BlockProgressOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new GuiBlockProgress(mc);

        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        ItemStack activeStack = mc.player.getUseItem();

        gui.setProgress(
                CastOptional.cast(activeStack.getItem(), ItemModularHandheld.class)
                        .map(item -> item.getBlockProgress(activeStack, mc.player))
                        .orElse(0f));

        gui.draw(event.getPoseStack());
    }
}
