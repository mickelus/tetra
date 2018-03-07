package se.mickelus.tetra.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.text.WordUtils;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableList;

public abstract class ItemModular extends TetraItem implements IItemModular, ICapabilityProvider {

    protected static final String repairCountKey = "repairCount";

    protected static final String cooledStrengthKey = "cooledStrength";

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
        NBTTagCompound stackTag = NBTHelper.getTag(stack);

        if (stackTag != null) {
            return Stream.concat(Arrays.stream(majorModuleKeys),Arrays.stream(minorModuleKeys))
                    .map(stackTag::getString)
                    .map(ItemUpgradeRegistry.instance::getModule)
                    .filter(Objects::nonNull)
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
        ItemModule[] modules = new ItemModule[minorModuleKeys.length];
        NBTTagCompound stackTag = NBTHelper.getTag(itemStack);

        for (int i = 0; i < minorModuleKeys.length; i++) {
            String moduleName = stackTag.getString(minorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
            modules[i] = module;
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

    public boolean hasModule(ItemStack itemStack, ItemModule module) {
        return getAllModules(itemStack).stream()
            .anyMatch(module::equals);
    }

    public ItemModule getModuleFromSlot(ItemStack itemStack, String slot) {
        return ItemUpgradeRegistry.instance.getModule(NBTHelper.getTag(itemStack).getString(slot));
    }

    @Override
    public java.util.Set<String> getToolClasses(ItemStack itemStack) {
        return super.getToolClasses(itemStack);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        return super.getHarvestLevel(stack, toolClass, player, blockState);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return super.getItemEnchantability(stack);
    }

    public void applyDamage(int amount, ItemStack itemStack, EntityLivingBase responsibleEntity) {
        if (itemStack.getItemDamage() + amount < itemStack.getMaxDamage()) {
            itemStack.damageItem(amount, responsibleEntity);
        } else {
            setDamage(itemStack, itemStack.getMaxDamage());
        }
    }

    public boolean isBroken(ItemStack itemStack) {
        return itemStack.getMaxDamage() != 0 && itemStack.getItemDamage() >= itemStack.getMaxDamage();
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
        if (isBroken(itemStack)) {
            tooltip.add("§4§o" + I18n.format("item.modular.broken"));
        }
    }

    /**
     * Returns an itemstack with the material required for the next repair attempt. Rotates between materials required
     * for different modules
     * @param itemStack The itemstack for the modular item
     * @return An itemstack representing the material required for the repair
     */
    public ItemStack getRepairMaterial(ItemStack itemStack) {
        List<ItemModule> modules = getAllModules(itemStack).stream()
                .filter(itemModule -> itemModule.getRepairMaterial(itemStack) != null)
                .collect(Collectors.toList());
        return modules.get(getRepairCount(itemStack) % modules.size()).getRepairMaterial(itemStack);
    }

    /**
     * Returns the amount of durability restored by the next repair attemt.
     * @param itemStack The itemstack for the modular item
     * @return
     */
    public int getRepairAmount(ItemStack itemStack) {
        List<ItemModule> modules = getAllModules(itemStack).stream()
                .filter(itemModule -> itemModule.getRepairMaterial(itemStack) != null)
                .collect(Collectors.toList());
        if (modules.size() > 0) {
            return modules.get(getRepairCount(itemStack) % modules.size()).getRepairAmount(itemStack);
        } else {
            return 0;
        }
    }

    public Collection<Capability> getRepairRequiredCapabilities(ItemStack itemStack) {
        List<ItemModule> modules = getAllModules(itemStack).stream()
                .filter(itemModule -> itemModule.getRepairMaterial(itemStack) != null)
                .collect(Collectors.toList());
        if (modules.size() > 0) {
            return modules.get(getRepairCount(itemStack) % modules.size()).getRequiredCapabilities(getRepairMaterial(itemStack));
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public int getRepairRequiredCapabilityLevel(ItemStack itemStack, Capability capability) {
        List<ItemModule> modules = getAllModules(itemStack).stream()
                .filter(itemModule -> itemModule.getRepairMaterial(itemStack) != null)
                .collect(Collectors.toList());
        if (modules.size() > 0) {
            int level = modules.get(getRepairCount(itemStack) % modules.size()).getRequiredCapabilityLevel(getRepairMaterial(itemStack), capability);
            return level > 1 ? level - 1 : 1;
        } else {
            return 0;
        }
    }

    private int getRepairCount(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getInteger(repairCountKey);
    }

    private void incrementRepairCount(ItemStack itemStack) {
        NBTTagCompound tag = NBTHelper.getTag(itemStack);
        tag.setInteger(repairCountKey, tag.getInteger(repairCountKey) + 1);
    }

    public void repair(ItemStack itemStack) {
        setDamage(itemStack, getDamage(itemStack) - getRepairAmount(itemStack));

        incrementRepairCount(itemStack);
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getCapabilityLevel(itemStack, capability))
                .max(Integer::compare)
                .orElse(0);
    }

    @Override
    public Collection<Capability> getCapabilities(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .flatMap(module -> ((Collection<Capability>)module.getCapabilities(itemStack)).stream())
                .collect(Collectors.toSet());
    }

    public String[] getImprovements(ItemStack itemStack) {
        return Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .flatMap(module -> Arrays.stream(module.getImprovements(itemStack)))
                .toArray(String[]::new);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        // todo: since getItemStackDisplayName is called on the server we cannot use the new I18n service
        if (FMLCommonHandler.instance().getEffectiveSide().equals(Side.SERVER)) {
            return "";
        }
        String name = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(module -> module.getItemNamePriority(itemStack)))
                .map(module -> module.getItemName(itemStack))
                .filter(Objects::nonNull)
                .findFirst().orElse("");

        String prefixes = Stream.concat(
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> improvement + ".prefix")
                        .filter(I18n::hasKey)
                        .map(I18n::format),
                getAllModules(itemStack).stream()
                        .sorted(Comparator.comparing(module -> module.getItemPrefixPriority(itemStack)))
                        .map(module -> module.getItemPrefix(itemStack))
                        .filter(Objects::nonNull)
                )
                .limit(2)
                .reduce("", (result, prefix) -> result + prefix + " ");
        return WordUtils.capitalize(prefixes + name);
    }
}
