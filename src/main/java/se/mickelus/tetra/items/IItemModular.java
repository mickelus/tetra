package se.mickelus.tetra.items;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

public interface IItemModular {

    public boolean isModuleRequired(String moduleSlot);
    public int getNumMajorModules();
    public String[] getMajorModuleKeys();
    public String[] getMajorModuleNames();
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack);

    public int getNumMinorModules();
    public String[] getMinorModuleKeys();
    public String[] getMinorModuleNames();
    public ItemModule[] getMinorModules(ItemStack itemStack);

    public ImmutableList<ResourceLocation> getTextures(ItemStack itemStack);
}
