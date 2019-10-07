package se.mickelus.tetra;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

public class NBTHelper {
    private static final String stacksKey = "stacks";
    private static final String slotKey = "slot";

    public static CompoundNBT getTag(ItemStack stack) {
        if(stack == null) {
            return new CompoundNBT();
        }

        if (!stack.hasTag()) {
            stack.setTag(new CompoundNBT());
        }

        return stack.getTag();
    }

    public static void writeItemStacks(NonNullList<ItemStack> itemStacks, CompoundNBT nbt) {
        ListNBT ListNBT = new ListNBT();
        for (int i = 0; i < itemStacks.size(); i++) {
            if (!itemStacks.get(i).isEmpty()) {
                CompoundNBT compoundNBT = new CompoundNBT();

                compoundNBT.putByte(slotKey, (byte) i);
                itemStacks.get(i).write(compoundNBT);

                ListNBT.add(compoundNBT);
            }
        }

        nbt.put(stacksKey, ListNBT);
    }

    public static void readItemStacks(CompoundNBT nbt, NonNullList<ItemStack> itemStacks) {
        if (nbt.contains(stacksKey)) {
            ListNBT tagList = nbt.getList(stacksKey, 10);

            for (int i = 0; i < tagList.size(); ++i) {
                CompoundNBT compoundNBT = tagList.getCompound(i);
                int slot = compoundNBT.getByte(slotKey) & 255;

                if (slot < itemStacks.size()) {
                    itemStacks.set(slot, ItemStack.read(compoundNBT));
                }
            }
        }
    }
}
