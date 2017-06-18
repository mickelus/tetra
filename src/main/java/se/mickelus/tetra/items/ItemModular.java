package se.mickelus.tetra.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.LinkedList;

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
        LinkedList<ItemModule> modules = new LinkedList<>();

        for (String moduleKey : majorModuleKeys) {
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
            if (module != null) {
                modules.add(module);
            }
        }

        for (String moduleKey : minorModuleKeys) {
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
            if (module != null) {
                modules.add(module);
            }
        }

        return modules.toArray(new ItemModule[modules.size()]);
    }

    @Override
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        NBTTagCompound stackTag = itemStack.getTagCompound();

        for (int i = 0; i < majorModuleKeys.length; i++) {
            String moduleKey = stackTag.getString(majorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
            if (module instanceof ItemModuleMajor) {
                modules[i] = (ItemModuleMajor) module;
            }
        }

        return modules;
    }

    @Override
    public ItemModule[] getMinorModules(ItemStack itemStack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[minorModuleKeys.length];
        NBTTagCompound stackTag = itemStack.getTagCompound();

        for (int i = 0; i < minorModuleKeys.length; i++) {
            String moduleKey = stackTag.getString(minorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
            modules[i] = (ItemModuleMajor) module;
        }

        return modules;
    }

    @Override
    public int getNumMajorModules() {
        return minorModuleNames.length;
    }

    @Override
    public String[] getMajorModuleNames() {
        return majorModuleNames;
    }

    @Override
    public int getNumMinorModules() {
        return minorModuleNames.length;
    }

    @Override
    public String[] getMinorModuleNames() {
        return minorModuleNames;
    }
}
