package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;

public abstract class ItemModuleMajor extends ItemModule {

    @Override
    public int getIntegrity(ItemStack stack) {
        return 0;
    }

    @Override
    public int getDurability(ItemStack stack) {
        return 0;
    }
}
