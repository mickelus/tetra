package se.mickelus.tetra.items.toolbelt.booster;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;

public class OverlayBooster {

    private final Minecraft mc;
    private final OverlayGuiBooster gui;

    public OverlayBooster(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiBooster(mc);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        float fuelPercent = -1;

        if (event.side != Side.CLIENT) {
            return;
        }

        ItemStack itemStack = UtilToolbelt.findToolbelt(event.player);
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
