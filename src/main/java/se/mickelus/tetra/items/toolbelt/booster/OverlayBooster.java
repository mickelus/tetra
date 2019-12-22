package se.mickelus.tetra.items.toolbelt.booster;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.toolbelt.ToolbeltHelper;

public class OverlayBooster {

    private final Minecraft mc;
    private final OverlayGuiBooster gui;

    public OverlayBooster(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiBooster(mc);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        float fuelPercent = 0;

        if (!event.side.isClient()) {
            return;
        }

        ItemStack itemStack = ToolbeltHelper.findToolbelt(event.player);
        if (UtilBooster.canBoost(itemStack)) {
            fuelPercent = UtilBooster.getFuelPercent(NBTHelper.getTag(itemStack));
        }

        gui.setFuel(fuelPercent);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        gui.draw();
    }
}
