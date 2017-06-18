package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

public class BladeModule extends ItemModuleMajor {

    public static final String key = "basic_blade";

    public static BladeModule instance;

    public BladeModule() {
        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public String getName(ItemStack stack) {
        return "Wooden blade";
    }

    @Override
    public void addModule(ItemStack targetStack, ItemStack[] materials) {
        NBTTagCompound tag = targetStack.getTagCompound();

        tag.setString(ItemSwordModular.bladeKey, key);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }
}
