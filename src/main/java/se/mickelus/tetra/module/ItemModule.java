package se.mickelus.tetra.module;

import com.google.common.collect.Multimap;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import org.apache.commons.lang3.StringUtils;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.schematic.RepairDefinition;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.util.CastOptional;

import java.util.*;

public abstract class ItemModule implements IToolProvider {

    protected VariantData[] variantData = new VariantData[0];

    protected TweakData[] tweaks = new TweakData[0];

    protected final String slotTagKey;
    protected final String moduleKey;
    protected final String variantTagKey;

    protected Priority renderLayer = Priority.BASE;

    public static final float repairLevelFactor = 10;

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

    public void addModule(ItemStack targetStack, String variantKey, Player player) {
        CompoundTag tag = targetStack.getOrCreateTag();

        tag.putString(slotTagKey, moduleKey);
        tag.putString(this.variantTagKey, variantKey);
    }

    public ItemStack[] removeModule(ItemStack targetStack) {
        CompoundTag tag = targetStack.getOrCreateTag();

        tag.remove(slotTagKey);
        tag.remove(variantTagKey);

        return new ItemStack[0];
    }

    public void postRemove(ItemStack targetStack, Player player) { }

    public VariantData[] getVariantData() {
        return variantData;
    }

    public VariantData getVariantData(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getString(variantTagKey))
                .map(key -> getVariantData(key))
                .orElseGet(this::getDefaultData);
    }

    public VariantData getVariantData(String variantKey) {
        return Arrays.stream(variantData)
                .filter(moduleData -> moduleData.key.equals(variantKey))
                .findAny()
                .orElseGet(this::getDefaultData);
    }

    public ItemProperties getProperties(ItemStack itemStack) {
        // merging identity here to flip integrity usage over, so that usage/available accumulates properly
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getProperties(getTweakStep(itemStack, tweak)))
                .reduce(ItemProperties.merge(new ItemProperties(), getVariantData(itemStack)), ItemProperties::merge);
    }

    public VariantData getDefaultData() {
        return variantData.length > 0 ? variantData[0] : new VariantData();
    }

    public static String getName(String moduleKey, String variantKey) {
        if (I18n.exists("tetra.variant." + variantKey)) {
            return I18n.get("tetra.variant." + variantKey);
        }

        if (I18n.exists("tetra.module." + moduleKey + ".material_name")) {
            String variant = variantKey.substring(variantKey.indexOf('/') + 1);
            if (I18n.exists("tetra.material." + variant + ".prefix")) {
                return StringUtils.capitalize(I18n.get("tetra.module." + moduleKey + ".material_name",
                        I18n.get("tetra.material." + variant + ".prefix")).toLowerCase());
            }
        }

        return I18n.get("tetra.variant." + variantKey);
    }

    public String getName(ItemStack itemStack) {
        String key = getVariantData(itemStack).key;
        return getName(getUnlocalizedName(), key);
    }

    public String getDescription(ItemStack itemStack) {
        String descriptionKey = "tetra.variant." + getVariantData(itemStack).key + ".description";
        if (I18n.exists(descriptionKey)) {
            return I18n.get(descriptionKey);
        }
        return I18n.get("tetra.module." + getUnlocalizedName() + ".description");
    }

    public String getItemName(ItemStack itemStack) {
        String variantItemNameKey = "tetra.variant." + getVariantData(itemStack).key + ".item_name";
        if (I18n.exists(variantItemNameKey)) {
            return I18n.get(variantItemNameKey);
        }

        String moduleItemNameKey = "tetra.module." + getUnlocalizedName() + ".item_name";
        if (I18n.exists(moduleItemNameKey)) {
            return I18n.get(moduleItemNameKey);
        }

        return null;
    }

    public Priority getItemNamePriority(ItemStack itemStack) {
        return Priority.BASE;
    }

    public String getItemPrefix(ItemStack itemStack) {
        String key = getVariantData(itemStack).key;
        String variantPrefixKey = "tetra.variant." + key + ".prefix";
        if (I18n.exists(variantPrefixKey)) {
            return I18n.get(variantPrefixKey);
        }

        String modulePrefixKey = "tetra.module." + getUnlocalizedName() + ".prefix";
        if (I18n.exists(modulePrefixKey)) {
            String prefix = I18n.get(modulePrefixKey);

            // for when module should derive the prefix from the material, slight hack
            if (prefix.startsWith("Format error:")) {
                String variant = key.substring(key.indexOf('/') + 1);
                return StringUtils.capitalize(
                        I18n.get(modulePrefixKey, I18n.get("tetra.material." + variant + ".prefix").toLowerCase()));
            }

            return prefix;
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
        return Math.max(getProperties(itemStack).integrity, 0);
    }

    /**
     * Returns the integrity cost of this module. Split into two methods as modules with improvements may have
     * internal gains/costs which should be visible.
     *
     * @param itemStack An itemstack containing module data for this module
     * @return Integrity cost of this module, excluding internal gains
     */
    public int getIntegrityCost(ItemStack itemStack) {
        return Math.max(getProperties(itemStack).integrityUsage, 0);
    }

    public int getMagicCapacity(ItemStack itemStack) {
        return getMagicCapacityGain(itemStack) - getMagicCapacityCost(itemStack);
    }

    public int getMagicCapacityGain(ItemStack itemStack) {
        int magicCapacity = getVariantData(itemStack).magicCapacity;
        if (magicCapacity > 0 ) {
            float stabilityMultiplier = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                    .map(item -> item.getStabilityModifier(itemStack))
                    .orElse(1f);

            return Math.round(magicCapacity * ConfigHandler.magicCapacityMultiplier.get().floatValue() * stabilityMultiplier);
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

    public float getDestabilizationChance(ItemStack itemStack, float probabilityMultiplier) {
        return getDestabilizationChance(-getMagicCapacity(itemStack), getMagicCapacityGain(itemStack), probabilityMultiplier);
    }

    public float getDestabilizationChance(int instability, int capacity, float probabilityMultiplier) {
        return Math.max(probabilityMultiplier *  instability / capacity, 0);
    }

    public int getDurability(ItemStack itemStack) {
        return getProperties(itemStack).durability;
    }

    public float getDurabilityMultiplier(ItemStack itemStack) {
        return getProperties(itemStack).durabilityMultiplier;
    }

    public Collection<RepairDefinition> getRepairDefinitions(ItemStack itemStack) {
        return RepairRegistry.instance.getDefinitions(getVariantData(itemStack).key);
    }

    public RepairDefinition getRepairDefinition(ItemStack itemStack, ItemStack materialStack) {
        return RepairRegistry.instance.getDefinitions(getVariantData(itemStack).key).stream()
                .filter(definition -> definition.material.isValid())
                .filter(definition -> definition.material.getPredicate().matches(materialStack))
                .findFirst()
                .orElse(null);
    }

    public Collection<ToolAction> getRepairRequiredTools(ItemStack itemStack, ItemStack materialStack) {
        return Optional.ofNullable(getRepairDefinition(itemStack, materialStack))
                .map(definition -> definition.requiredTools.getValues())
                .orElseGet(Collections::emptySet);
    }

    public Map<ToolAction, Integer> getRepairRequiredToolLevels(ItemStack itemStack, ItemStack materialStack) {
        return Optional.ofNullable(getRepairDefinition(itemStack, materialStack))
                .map(definition -> definition.requiredTools.getLevelMap())
                .orElseGet(Collections::emptyMap);
    }

    public int getRepairRequiredToolLevel(ItemStack itemStack, ItemStack materialStack, ToolAction tool) {
        return Optional.ofNullable(getRepairDefinition(itemStack, materialStack))
                .map(definition -> definition.requiredTools.getLevel(tool))
                .orElse(0);
    }

    public int getRepairExperienceCost(ItemStack itemStack) {
        return Optional.of(getDestabilizationChance(itemStack, 1))
                .map(capacity -> capacity * repairLevelFactor)
                .map(Mth::ceil)
                .map(capacity -> Math.max(0, capacity))
                .orElse(0);
    }

    public boolean isTweakable(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            String variant = itemStack.getTag().getString(variantTagKey);
            return Arrays.stream(tweaks)
                    .anyMatch(data -> variant.equals(data.variant));
        }

        return false;
    }

    public TweakData[] getTweaks(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            String variant = itemStack.getTag().getString(variantTagKey);
            return Arrays.stream(tweaks)
                    .filter(tweak -> variant.equals(tweak.variant))
                    .toArray(TweakData[]::new);
        }
        return new TweakData[0];
    }

    public boolean hasTweak(ItemStack itemStack, String tweakKey) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.key)
                .anyMatch(tweakKey::equals);
    }

    public int getTweakStep(ItemStack itemStack, TweakData tweak) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getInt(slotTagKey + ":" + tweak.key))
                .map(step -> Mth.clamp(step, -tweak.steps,  tweak.steps))
                .orElse(0);
    }

    public void setTweakStep(ItemStack itemStack, String tweakKey, int step) {
        itemStack.getOrCreateTag().putInt(slotTagKey + ":" + tweakKey, step);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getAttributeModifiers(getTweakStep(itemStack, tweak)))
                .filter(Objects::nonNull)
                .reduce(getVariantData(itemStack).attributes, AttributeHelper::merge);
    }

    public double getDamageModifier(ItemStack itemStack) {
        return Optional.ofNullable(getAttributeModifiers(itemStack))
                .map(modifiers -> modifiers.get(Attributes.ATTACK_DAMAGE))
                .map(AttributeHelper::getAdditionAmount)
                .orElse(0d);
    }

    public double getDamageMultiplierModifier(ItemStack itemStack) {
        return Optional.ofNullable(getAttributeModifiers(itemStack))
                .map(modifiers -> modifiers.get(Attributes.ATTACK_DAMAGE))
                .map(AttributeHelper::getMultiplyAmount)
                .orElse(1d);
    }

    public double getSpeedModifier(ItemStack itemStack) {
        return Optional.ofNullable(getAttributeModifiers(itemStack))
                .map(modifiers -> modifiers.get(Attributes.ATTACK_SPEED))
                .map(AttributeHelper::getAdditionAmount)
                .orElse(0d);
    }

    public double getSpeedMultiplierModifier(ItemStack itemStack) {
        return Optional.ofNullable(getAttributeModifiers(itemStack))
                .map(modifiers -> modifiers.get(Attributes.ATTACK_SPEED))
                .map(AttributeHelper::getMultiplyAmount)
                .orElse(1d);
    }

    public double getRangeModifier(ItemStack itemStack) {
        return Optional.ofNullable(getAttributeModifiers(itemStack))
                .map(modifiers -> modifiers.get(ForgeMod.REACH_DISTANCE.get()))
                .map(AttributeHelper::getAdditionAmount)
                .orElse(0d);
    }

    public ModuleModel[] getModels(ItemStack itemStack) {
        return getVariantData(itemStack).models;
    }

    public Priority getRenderLayer() {
        return renderLayer;
    }

    public int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        return Optional.ofNullable(getEffectData(itemStack))
                .map(data -> data.getLevel(effect))
                .orElse(0);
    }

    public float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        return Optional.ofNullable(getEffectData(itemStack))
                .map(data -> data.getEfficiency(effect))
                .orElse(0f);
    }

    public Collection<ItemEffect> getEffects(ItemStack itemStack) {
        return Optional.ofNullable(getEffectData(itemStack))
                .map(TierData::getValues)
                .orElseGet(Collections::emptySet);
    }

    public EffectData getEffectData(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getEffectData(getTweakStep(itemStack, tweak)))
                .filter(Objects::nonNull)
                .reduce(getVariantData(itemStack).effects, EffectData::merge);
    }

    @Override
    public boolean canProvideTools(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getToolLevel(ItemStack itemStack, ToolAction tool) {
        return Optional.ofNullable(getToolData(itemStack))
                .map(data -> data.getLevel(tool))
                .orElse(0);
    }

    @Override
    public float getToolEfficiency(ItemStack itemStack, ToolAction tool) {
        return Optional.ofNullable(getToolData(itemStack))
                .map(data -> data.getEfficiency(tool))
                .orElse(0f);
    }

    @Override
    public Set<ToolAction> getTools(ItemStack itemStack) {
        return Optional.ofNullable(getToolData(itemStack))
                .map(TierData::getValues)
                .orElseGet(Collections::emptySet);
    }

    @Override
    public Map<ToolAction, Integer> getToolLevels(ItemStack itemStack) {
        return Optional.ofNullable(getToolData(itemStack))
                .map(TierData::getLevelMap)
                .orElseGet(Collections::emptyMap);
    }

    public ToolData getToolData(ItemStack itemStack) {
        return Arrays.stream(getTweaks(itemStack))
                .map(tweak -> tweak.getToolData(getTweakStep(itemStack, tweak)))
                .filter(Objects::nonNull)
                .reduce(getVariantData(itemStack).tools, ToolData::merge);
    }
}
