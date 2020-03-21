package se.mickelus.tetra.module;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.module.data.ModuleVariantData;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.data.TweakData;
import se.mickelus.tetra.module.schema.Material;
import se.mickelus.tetra.module.schema.RepairDefinition;

import javax.annotation.Nullable;

public abstract class ItemModule implements ICapabilityProvider {

    protected ModuleVariantData[] variantData = new ModuleVariantData[0];

    protected TweakData[] tweaks = new TweakData[0];

    protected final String slotTagKey;
    protected final String moduleKey;
    protected final String variantTagKey;

    protected Priority renderLayer = Priority.BASE;

    public ItemModule(String slotKey, String moduleKey) {
        this.slotTagKey = slotKey;
        this.moduleKey = moduleKey;
        this.variantTagKey = moduleKey + "_material";
    }

    public String getKey() {
        return moduleKey;
    }

    public String getUnlocalizedName() {
        return moduleKey;
    }

    public String getSlot() {
        return slotTagKey;
    }

    public void addModule(ItemStack targetStack, String variantKey, PlayerEntity player) {
        CompoundNBT tag = NBTHelper.getTag(targetStack);

        tag.putString(slotTagKey, moduleKey);
        tag.putString(this.variantTagKey, variantKey);
    }

    public ItemStack[] removeModule(ItemStack targetStack) {
        CompoundNBT tag = NBTHelper.getTag(targetStack);

        tag.remove(slotTagKey);
        tag.remove(variantTagKey);

        return new ItemStack[0];
    }

    public void postRemove(ItemStack targetStack, PlayerEntity player) { }

    public ModuleVariantData[] getVariantData() {
        return variantData;
    }

    public ModuleVariantData getVariantData(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        String variantKey = tag.getString(variantTagKey);

        return getVariantData(variantKey);
    }

    public ModuleVariantData getVariantData(String variantKey) {
        return Arrays.stream(variantData)
                .filter(moduleData -> moduleData.key.equals(variantKey))
                .findAny().orElseGet(this::getDefaultData);
    }

    public ModuleVariantData getDefaultData() {
        return variantData.length > 0 ? variantData[0] : new ModuleVariantData();
    }

    public String getName(ItemStack itemStack) {
        return I18n.format(getVariantData(itemStack).key);
    }

    public String getDescription(ItemStack itemStack) {
        String dataKey = getVariantData(itemStack).key;
        if (I18n.hasKey(dataKey + ".description")) {
            return I18n.format(dataKey + ".description");
        }
        return I18n.format(getUnlocalizedName() + ".description");
    }

    public String getItemName(ItemStack itemStack) {
        String name = getVariantData(itemStack).key + ".name";
        if (I18n.hasKey(name)) {
            return I18n.format(name);
        } else if (I18n.hasKey(getUnlocalizedName() + ".item_name")) {
            return I18n.format(getUnlocalizedName() + ".item_name");
        }
        return null;
    }

    public Priority getItemNamePriority(ItemStack itemStack) {
        return Priority.BASE;
    }

    public String getItemPrefix(ItemStack itemStack) {
        String prefix = getVariantData(itemStack).key + ".prefix";
        if (I18n.hasKey(prefix)) {
            return I18n.format(prefix);
        } else if (I18n.hasKey(getUnlocalizedName() + ".prefix")) {
            return I18n.format(getUnlocalizedName() + ".prefix");
        }
        return null;
    }

    public Priority getItemPrefixPriority(ItemStack itemStack) {
        return Priority.BASE;
    }


    /**
     * Returns the integrity gained from this module. Split into two methods as modules with improvements may have
     * internal gains/costs which should be visible.
     *
     * @param itemStack An itemstack containing module data for this module
     * @return Integrity gained from this module, excluding internal costs
     */
    public int getIntegrityGain(ItemStack itemStack) {
        int integrity = getVariantData(itemStack).integrity;
        if (integrity > 0 ) {
            return integrity;
        }
        return 0;
    }

    /**
     * Returns the integrity cost of this module. Split into two methods as modules with improvements may have
     * internal gains/costs which should be visible.
     *
     * @param itemStack An itemstack containing module data for this module
     * @return Integrity cost of this module, excluding internal gains
     */
    public int getIntegrityCost(ItemStack itemStack) {
        int integrity = getVariantData(itemStack).integrity;
        if (integrity < 0 ) {
            return integrity;
        }
        return 0;
    }

    public int getMagicCapacity(ItemStack itemStack) {
        return getMagicCapacityGain(itemStack) - getMagicCapacityCost(itemStack);
    }

    public int getMagicCapacityGain(ItemStack itemStack) {
        int magicCapacity = getVariantData(itemStack).magicCapacity;
        if (magicCapacity > 0 ) {
            return Math.round(magicCapacity * ConfigHandler.magicCapacityMultiplier.get().floatValue());
        }
        return 0;
    }

    public int getMagicCapacityCost(ItemStack itemStack) {
        int magicCapacity = getVariantData(itemStack).magicCapacity;
        if (magicCapacity < 0 ) {
            return -magicCapacity;
        }
        return 0;
    }

    public int getDurability(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .mapToInt(tweak -> tweak.getDurability(getTweakStep(itemStack, tweak)))
                .sum() + getVariantData(itemStack).durability;
    }

    public float getDurabilityMultiplier(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getDurabilityMultiplier(getTweakStep(itemStack, tweak)))
                .reduce(getVariantData(itemStack).durabilityMultiplier, (a, b) -> a * b);
    }

    public Collection<RepairDefinition> getRepairDefinitions(ItemStack itemStack) {
        return RepairRegistry.instance.getDefinitions(getVariantData(itemStack).key);
    }

    public RepairDefinition getRepairDefinition(ItemStack itemStack, ItemStack materialStack) {
        return RepairRegistry.instance.getDefinitions(getVariantData(itemStack).key).stream()
                .filter(definition -> definition.material.predicate.test(materialStack))
                .findFirst()
                .orElse(null);
    }

    public Collection<Capability> getRepairRequiredCapabilities(ItemStack itemStack, ItemStack materialStack) {
        return Optional.ofNullable(getRepairDefinition(itemStack, materialStack))
                .map(definition -> definition.requiredCapabilities.getValues())
                .orElse(Collections.emptySet());
    }

    public int getRepairRequiredCapabilityLevel(ItemStack itemStack, ItemStack materialStack, Capability capability) {
        return Optional.ofNullable(getRepairDefinition(itemStack, materialStack))
                .map(definition -> definition.requiredCapabilities.getLevel(capability))
                .orElse(0);
    }

    public boolean isTweakable(ItemStack itemStack) {
        String variant = NBTHelper.getTag(itemStack).getString(this.variantTagKey);
        return Arrays.stream(tweaks)
                .anyMatch(data -> variant.equals(data.variant));
    }

    public TweakData[] getTweaks(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        String variant = tag.getString(this.variantTagKey);
        return Arrays.stream(tweaks)
                .filter(tweak -> variant.equals(tweak.variant))
                .toArray(TweakData[]::new);
    }

    public boolean hasTweak(ItemStack itemStack, String tweakKey) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.key)
                .anyMatch(tweakKey::equals);
    }

    public int getTweakStep(ItemStack itemStack, TweakData tweak) {
        return Math.max(Math.min(NBTHelper.getTag(itemStack).getInt(slotTagKey + ":" + tweak.key), tweak.steps), -tweak.steps);
    }

    public void setTweakStep(ItemStack itemStack, String tweakKey, int step) {
        NBTHelper.getTag(itemStack).putInt(slotTagKey + ":" + tweakKey, step);
    }

    public double getDamageModifier(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .mapToDouble(tweak -> tweak.getDamage(getTweakStep(itemStack, tweak)))
                .sum() + getVariantData(itemStack).damage;
    }

    public double getDamageMultiplierModifier(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getDamageMultiplier(getTweakStep(itemStack, tweak)))
                .reduce(getVariantData(itemStack).damageMultiplier, (a, b) -> a * b);
    }

    public double getSpeedModifier(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .mapToDouble(tweak -> tweak.getAttackSpeed(getTweakStep(itemStack, tweak)))
                .sum() + getVariantData(itemStack).attackSpeed;
    }

    public double getSpeedMultiplierModifier(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getAttackSpeedMultiplier(getTweakStep(itemStack, tweak)))
                .reduce(getVariantData(itemStack).attackSpeedMultiplier, (a, b) -> a * b);
    }

    public double getRangeModifier(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .mapToDouble(tweak -> tweak.getRange(getTweakStep(itemStack, tweak)))
                .sum() + getVariantData(itemStack).range;
    }

    public ModuleModel[] getModels(ItemStack itemStack) {
        return getVariantData(itemStack).models;
    }

    public Priority getRenderLayer() {
        return renderLayer;
    }

    public void hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {}

    public int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        return getVariantData(itemStack).effects.getLevel(effect);
    }

    public float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        return (float) Arrays.stream(getTweaks(itemStack))
                .mapToDouble(tweak -> tweak.getEffectEfficiency(effect, getTweakStep(itemStack, tweak)))
                .sum() + getVariantData(itemStack).effects.getEfficiency(effect);
    }

    public Collection<ItemEffect> getEffects(ItemStack itemStack) {
        return getVariantData(itemStack).effects.getValues();
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return getVariantData(itemStack).capabilities.getLevel(capability);
    }

    @Override
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability) {
        return (float) Arrays.stream(getTweaks(itemStack))
                .mapToDouble(tweak -> tweak.getCapabilityEfficiency(capability, getTweakStep(itemStack, tweak)))
                .sum() + getVariantData(itemStack).capabilities.getEfficiency(capability);
    }

    @Override
    public Collection<Capability> getCapabilities(ItemStack itemStack) {
        return getVariantData(itemStack).capabilities.getValues();
    }
}
