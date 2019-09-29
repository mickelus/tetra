package se.mickelus.tetra;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class NBTHelper {
    private static final String stacksKey = "stacks";
    private static final String slotKey = "slot";

    public static CompoundNBT getTag(ItemStack stack) {
        if(stack == null) {
            return new CompoundNBT();
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new CompoundNBT());
        }

        return stack.getTagCompound();
    }

    public static void writeItemStacks(NonNullList<ItemStack> itemStacks, CompoundNBT nbt) {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < itemStacks.size(); i++) {
            if (!itemStacks.get(i).isEmpty()) {
                CompoundNBT CompoundNBT = new CompoundNBT();

                CompoundNBT.setByte(slotKey, (byte) i);
                itemStacks.get(i).writeToNBT(CompoundNBT);

                nbttaglist.appendTag(CompoundNBT);
            }
        }

        nbt.setTag(stacksKey, nbttaglist);
    }

    public static void readItemStacks(CompoundNBT nbt, NonNullList<ItemStack> itemStacks) {
        if (nbt.hasKey(stacksKey)) {
            NBTTagList tagList = nbt.getTagList(stacksKey, 10);

            for (int i = 0; i < tagList.tagCount(); ++i) {
                CompoundNBT CompoundNBT = tagList.getCompoundTagAt(i);
                int slot = CompoundNBT.getByte(slotKey) & 255;

                if (slot < itemStacks.size()) {
                    itemStacks.set(slot, new ItemStack(CompoundNBT));
                }
            }
        }
    }
}
