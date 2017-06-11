package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;

public class UpgradeSchema {

    public String getName() {
        return "";
    }

    public String getDescription() {
        return "";
    }

    public int getNumMaterialSlots() {
        return 0;
    }

    public String getSlotName(int index) {
        return "";
    }

    public ItemModule createUpgrade(ItemStack[] itemStacks) {
        return null;
    }
}
