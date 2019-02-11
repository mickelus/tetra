package se.mickelus.tetra;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class NBTHelper {
    private static final String stacksKey = "stacks";
    private static final String slotKey = "slot";

    public static NBTTagCompound getTag(ItemStack stack) {
        if(stack == null) {
            return new NBTTagCompound();
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        return stack.getTagCompound();
    }

    public static void writeItemStacks(NonNullList<ItemStack> itemStacks, NBTTagCompound nbt) {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < itemStacks.size(); i++) {
            if (!itemStacks.get(i).isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.setByte(slotKey, (byte) i);
                itemStacks.get(i).writeToNBT(nbttagcompound);

                nbttaglist.appendTag(nbttagcompound);
            }
        }

        nbt.setTag(stacksKey, nbttaglist);
    }

    public static void readItemStacks(NBTTagCompound nbt, NonNullList<ItemStack> itemStacks) {
        if (nbt.hasKey(stacksKey)) {
            NBTTagList tagList = nbt.getTagList(stacksKey, 10);

            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
                int slot = nbttagcompound.getByte(slotKey) & 255;

                if (slot < itemStacks.size()) {
                    itemStacks.set(slot, new ItemStack(nbttagcompound));
                }
            }
        }
    }
}
