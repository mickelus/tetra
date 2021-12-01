package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
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

        if (TickEvent.Phase.START == event.phase && !event.side.isClient()) {
            return;
        }

        ItemStack itemStack = ToolbeltHelper.findToolbelt(event.player);
        if (UtilBooster.canBoost(itemStack)) {
            fuelPercent = UtilBooster.getFuelPercent(itemStack.getTag());
        }

        gui.setFuel(fuelPercent);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        gui.draw();
    }
}
