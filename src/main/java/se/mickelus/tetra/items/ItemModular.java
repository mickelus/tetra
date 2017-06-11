package se.mickelus.tetra.items;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemModule;

public abstract class ItemModular extends TetraItem implements IItemModular {
    protected String[] majorModuleNames;
    protected String[] majorModuleKeys;
    protected String[] minorModuleNames;
    protected String[] minorModuleKeys;

    protected int baseDurability = 0;
    protected int baseIntegrity = 0;

    @Override
    public int getMaxDamage(ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    public ItemModule[] getAllModules(ItemStack stack) {
        ItemModule[] modules = new ItemModule[majorModuleKeys.length + minorModuleKeys.length];



        return modules;
    }
}
