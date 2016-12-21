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

    private float fuelPercent = -1;

    public OverlayRocketBoots(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiRocketBoots(mc);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != Side.CLIENT) {
            return;
        }

        ItemStack stack;

        if (!UtilRocketBoots.hasBoots(event.player)) {
            fuelPercent = -1;
        } else {
            stack = event.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            fuelPercent = ItemRocketBoots.getFuelPercent(stack.getTagCompound());
        }

        gui.setFuel(fuelPercent);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        gui.draw();
    }
}
