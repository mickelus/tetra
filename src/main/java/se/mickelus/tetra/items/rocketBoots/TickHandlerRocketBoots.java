package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import se.mickelus.tetra.NBTHelper;

public class TickHandlerRocketBoots {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (UtilRocketBoots.hasBoots(event.player)) {
            tickItem(event.player, event.player.getItemStackFromSlot(EntityEquipmentSlot.FEET));
        }
    }

    public void tickItem(EntityPlayer player, ItemStack stack) {
        NBTTagCompound tag = NBTHelper.getTag(stack);
        boolean charged = tag.getBoolean(UtilRocketBoots.chargedKey);
        if (!player.isInWater() && UtilRocketBoots.isActive(tag) && UtilRocketBoots.hasFuel(tag, charged)) {
            if (charged) {
                UtilRocketBoots.boostPlayerCharged(player, tag);
            } else {
                UtilRocketBoots.boostPlayer(player, tag);
            }

            UtilRocketBoots.consumeFuel(tag, charged);
        } else {
            UtilRocketBoots.rechargeFuel(tag);
        }

        if (charged) {
            tag.setBoolean(UtilRocketBoots.chargedKey, false);
        }
    }
}
