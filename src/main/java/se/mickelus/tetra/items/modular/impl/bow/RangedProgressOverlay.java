package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import se.mickelus.tetra.compat.neoforge.client.gui.overlay.ForgeGui;
import se.mickelus.tetra.compat.neoforge.client.gui.overlay.IGuiOverlay;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RangedProgressOverlay implements IGuiOverlay {
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
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        this.gui.draw(graphics);
    }
}
