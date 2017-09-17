package se.mickelus.tetra.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.collect.ImmutableList;

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

    protected ItemModule[] getAllModules(ItemStack stack) {
        LinkedList<ItemModule> modules = new LinkedList<>();
        NBTTagCompound stackTag = stack.getTagCompound();

        if (stackTag != null) {
            for (String moduleKey : majorModuleKeys) {
                String moduleName = stackTag.getString(moduleKey);
                ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
                if (module != null) {
                    modules.add(module);
                }
            }

            for (String moduleKey : minorModuleKeys) {
                String moduleName = stackTag.getString(moduleKey);
                ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
                if (module != null) {
                    modules.add(module);
                }
            }
        }

        return modules.toArray(new ItemModule[modules.size()]);
    }

    @Override
    public ItemModuleMajor[] getMajorModules(ItemStack stack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        NBTTagCompound stackTag = stack.getTagCompound();

        for (int i = 0; i < majorModuleKeys.length; i++) {
            String moduleName = stackTag.getString(majorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
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
            String moduleName = stackTag.getString(minorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
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

	@Override
	public ImmutableList<ResourceLocation> getTextures(ItemStack itemStack) {
		List<ResourceLocation> textures = new LinkedList<>();

		for (ItemModule module : getAllModules(itemStack)) {
			Collections.addAll(textures, module.getTextures(itemStack));
		}

		return ImmutableList.copyOf(textures);
	}

    public java.util.Set<String> getToolClasses(ItemStack itemStack) {
        return super.getToolClasses(itemStack);
    }

    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        return super.getHarvestLevel(stack, toolClass, player, blockState);
    }

    public int getItemEnchantability(ItemStack stack) {
        return super.getItemEnchantability(stack);
    }
}
