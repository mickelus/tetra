package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.util.ItemStackTagHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OverlayBooster implements LayeredDraw.Layer {
    private final Minecraft mc;
    private final OverlayGuiBooster gui;

    public OverlayBooster(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiBooster(mc);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (mc.player != null) {
            float fuelPercent = 0;
            ItemStack itemStack = ToolbeltHelper.findToolbelt(mc.player);
            if (UtilBooster.canBoost(itemStack)) {
                fuelPercent = UtilBooster.getFuelPercent(ItemStackTagHelper.getOrCreateTag(itemStack));
            }

            gui.setFuel(fuelPercent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        this.gui.draw(graphics);
    }
}
