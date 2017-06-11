package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickHandlerRocketBoots {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (UtilRocketBoots.hasBoots(event.player)) {
            tickItem(event.player, event.player.getItemStackFromSlot(EntityEquipmentSlot.FEET));
        }
    }

    public void tickItem(EntityPlayer player, ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound tag = stack.getTagCompound();
        boolean charged = tag.getBoolean(UtilRocketBoots.chargedKey);
        if (UtilRocketBoots.isActive(tag) && UtilRocketBoots.hasFuel(tag, charged)) {
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
