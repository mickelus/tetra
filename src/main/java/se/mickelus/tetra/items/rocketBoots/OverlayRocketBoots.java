package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class OverlayRocketBoots {

    private final Minecraft mc;
    private final OverlayGuiRocketBoots gui;

    public OverlayRocketBoots(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiRocketBoots(mc);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        float fuelPercent = -1;

        if (event.side != Side.CLIENT) {
            return;
        }

        if (UtilRocketBoots.hasBoots(event.player)) {
            ItemStack stack = event.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

            fuelPercent = UtilRocketBoots.getFuelPercent(stack.getTagCompound());
        }

        gui.setFuel(fuelPercent);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        gui.draw();
    }
}
