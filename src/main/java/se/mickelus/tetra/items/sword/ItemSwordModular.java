package se.mickelus.tetra.items.sword;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.network.PacketPipeline;

public class ItemSwordModular extends ItemModular {

    private String[] majorModuleNames = new String[] {"Blade", "Hilt"};
    private String[] majorModuleKeys = new String[] {"sword:blade", "sword:hilt"};
    private String[] minorModuleNames = new String[] {"Guard", "Pommel", "Fuller"};
    private String[] minorModuleKeys = new String[] {"sword:guard", "sword:pommel", "sword:fuller"};

    public ItemSwordModular() {
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
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        NBTTagCompound stackTag = itemStack.getTagCompound();

        for (int i = 0; i < majorModuleKeys.length; i++) {
            NBTTagCompound moduleTag = stackTag.getCompoundTag(majorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModuleFromNBT(moduleTag);
            if (module instanceof ItemModuleMajor) {
                modules[i] = (ItemModuleMajor) module;
            }
        }

        return modules;
    }

    @Override
    public int getNumMinorModules() {
        return minorModuleNames.length;
    }

    @Override
    public String[] getMinorModuleNames() {
        return minorModuleNames;
    }

    @Override
    public ItemModule[] getMinorModules(ItemStack itemStack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[minorModuleKeys.length];
        NBTTagCompound stackTag = itemStack.getTagCompound();

        for (int i = 0; i < minorModuleKeys.length; i++) {
            NBTTagCompound moduleTag = stackTag.getCompoundTag(minorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModuleFromNBT(moduleTag);
            modules[i] = (ItemModuleMajor) module;
        }

        return modules;
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        ItemUpgradeRegistry.instance.registerPlaceholder(this::replaceSword);
    }

    private ItemStack replaceSword(ItemStack originalStack) {
        Item originalItem = originalStack.getItem();

        if (!(originalItem instanceof ItemSword)) {
            return null;
        }

        return new ItemStack(new ItemSwordModular());
    }
}
