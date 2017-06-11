package se.mickelus.tetra.items;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

public interface IItemModular {

    public int getNumMajorModules();
    public String[] getMajorModuleNames();
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack);

    public int getNumMinorModules();
    public String[] getMinorModuleNames();
    public ItemModule[] getMinorModules(ItemStack itemStack);
}
