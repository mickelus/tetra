package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class UtilRocketBoots {

    public static boolean hasBoots(EntityPlayer player) {
        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

        return stack != null && stack.getItem() instanceof ItemRocketBoots && stack.hasTagCompound();
    }

}
