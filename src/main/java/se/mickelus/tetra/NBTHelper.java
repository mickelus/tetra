package se.mickelus.tetra;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NBTHelper {
    public static NBTTagCompound getTag(ItemStack stack) {
        if(stack == null || !stack.hasTagCompound()) {
            return new NBTTagCompound();
        }

        return stack.getTagCompound();
    }
}
