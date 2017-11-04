package se.mickelus.tetra.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        return getAllModules(stack).stream()
                .map(itemModule -> itemModule.getDurability(stack))
                .reduce(0, Integer::sum) + baseDurability;
    }

    public static int getIntegrityGain(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModular) {
            return ((ItemModular) itemStack.getItem()).getAllModules(itemStack).stream()
                    .map(module -> module.getIntegrityGain(itemStack))
                    .reduce(0, Integer::sum);
        }
        return 0;
    }

    public static int getIntegrityCost(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModular) {
            return ((ItemModular) itemStack.getItem()).getAllModules(itemStack).stream()
                    .map(module -> module.getIntegrityCost(itemStack))
                    .reduce(0, Integer::sum);
        }
        return 0;
    }

    protected Collection<ItemModule> getAllModules(ItemStack stack) {
        NBTTagCompound stackTag = stack.getTagCompound();

        if (stackTag != null) {
            return Stream.concat(Arrays.stream(majorModuleKeys),Arrays.stream(minorModuleKeys))
                    .map(stackTag::getString)
                    .map(ItemUpgradeRegistry.instance::getModule)
                    .filter(itemModule -> itemModule != null)
                    .collect(Collectors.toList());
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        NBTTagCompound stackTag = NBTHelper.getTag(itemStack);

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
        NBTTagCompound stackTag = NBTHelper.getTag(itemStack);

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

        return getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getTextures(itemStack)))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
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
