package se.mickelus.tetra.module;


import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.NBTHelper;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ItemModuleMajor extends ItemModule {

    protected ImprovementData[] improvements = new ImprovementData[0];

    public static final String settleImprovement = "settled";
    public static final String arrestedImprovement = "arrested";

    protected int settleMax = 0;
    private String settleProgressKey = "/settle_progress";

    public ItemModuleMajor(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        settleProgressKey = getSlot() + settleProgressKey;
    }

    public void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        int settleMaxCount = getSettleMaxCount(itemStack);
        if (settleMaxCount == 0) {
            return;
        }

        CompoundNBT tag = NBTHelper.getTag(itemStack);
        int settleLevel = getImprovementLevel(itemStack, settleImprovement);

        if (settleLevel < settleMaxCount && (getImprovementLevel(itemStack, arrestedImprovement) == -1)) {
            int settleProgress = getSettleProgress(itemStack);

            settleProgress -= multiplier;
            tag.putInt(settleProgressKey, settleProgress);
            if (settleProgress <= 0) {
                addImprovement(itemStack, settleImprovement, settleLevel == -1 ? 1 : settleLevel + 1);
                tag.remove(settleProgressKey);

                if (entity instanceof ServerPlayerEntity) {
                    PacketHandler.sendTo(new SettlePacket(itemStack, getSlot()), (ServerPlayerEntity) entity);
                }
            }
        }
    }

    /**
     * Returns the remaining number of times the item has to be used before this module will settle.
     * @param itemStack The itemstack which the module is present on
     * @return
     */
    public int getSettleProgress(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        if (tag.contains(settleProgressKey)) {
            return tag.getInt(settleProgressKey);
        }

        return getSettleLimit(itemStack);
    }

    /**
     * Returns the total number of times the item has to be used before this module will settle.
     * @param itemStack The itemstack which the module is present on
     * @return
     */
    public int getSettleLimit(ItemStack itemStack) {
        return (int) (( ConfigHandler.settleLimitBase.get() + getDurability(itemStack) * ConfigHandler.settleLimitDurabilityMultiplier.get())
                * Math.max(getImprovementLevel(itemStack, settleImprovement) * ConfigHandler.settleLimitLevelMultiplier.get(), 1f));
    }

    /**
     * Returns the total number of times the item has to be used before this module will settle.
     * @param itemStack The itemstack which the module is present on
     * @return
     */
    public int getSettleMaxCount(ItemStack itemStack) {
        if (settleMax == 0) {
            return 0;
        }

        int integrity = getVariantData(itemStack).integrity;
        if (integrity <= -4 || integrity >= 6) {
            return settleMax;
        } else if (integrity != 0) {
            return 1;
        }

        return 0;
    }

    protected void clearProgression(ItemStack itemStack) {
        NBTHelper.getTag(itemStack).remove(String.format(settleProgressKey, getSlot()));
    }

    public int getImprovementLevel(ItemStack itemStack, String improvementKey) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        if (tag.contains(slotTagKey + ":" + improvementKey)) {
            return tag.getInt(slotTagKey + ":" + improvementKey);
        }
        return -1;
    }

    public ImprovementData getImprovement(ItemStack itemStack, String improvementKey) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        return Arrays.stream(improvements)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .filter(improvement -> tag.contains(slotTagKey + ":" + improvement.key))
                .filter(improvement -> improvement.level == tag.getInt(slotTagKey + ":" + improvement.key))
                .findAny()
                .orElse(null);
    }

    public ImprovementData[] getImprovements(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        return Arrays.stream(improvements)
            .filter(improvement -> tag.contains(slotTagKey + ":" + improvement.key))
            .filter(improvement -> improvement.level == tag.getInt(slotTagKey + ":" + improvement.key))
            .toArray(ImprovementData[]::new);
    }

    public boolean acceptsImprovement(String improvementKey) {
        return Arrays.stream(improvements)
                .map(improvement -> improvement.key)
                .anyMatch(improvementKey::equals);
    }

    public boolean acceptsImprovementLevel(String improvementKey, int level) {
        return Arrays.stream(improvements)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .anyMatch(improvement -> level == improvement.level);
    }

    public void addImprovement(ItemStack itemStack, String improvementKey, int level) {
        removeCollidingImprovements(itemStack, improvementKey, level);
        NBTHelper.getTag(itemStack).putInt(slotTagKey + ":" + improvementKey, level);
    }

    public static void addImprovement(ItemStack itemStack, String slot, String improvement, int level) {
        ModularItem item = (ModularItem) itemStack.getItem();
        CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                .ifPresent(module -> module.addImprovement(itemStack, improvement, level));
    }

    public void removeCollidingImprovements(ItemStack itemStack, String improvementKey, int level) {
        Arrays.stream(improvements)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .filter(improvement -> level == improvement.level)
                .filter(improvement -> improvement.group != null)
                .map(improvement -> improvement.group)
                .findFirst()
                .ifPresent(group -> Arrays.stream(getImprovements(itemStack))
                        .filter(improvement -> group.equals(improvement.group))
                        .forEach(improvement -> removeImprovement(itemStack, slotTagKey, improvement.key)));
    }

    public void removeImprovement(ItemStack itemStack, String improvement) {
        removeImprovement(itemStack, slotTagKey, improvement);
    }

    public static void removeImprovement(ItemStack itemStack, String slot, String improvement) {
        NBTHelper.getTag(itemStack).remove(slot + ":" + improvement);
    }

    public void removeEnchantments(ItemStack itemStack) {
        Arrays.stream(improvements)
                .filter(improvement -> improvement.enchantment)
                .forEach(improvement -> removeImprovement(itemStack, improvement.key));
    }

    @Override
    public boolean isTweakable(ItemStack itemStack) {
        String[] improvementKeys = Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.key)
                .toArray(String[]::new);

        return Arrays.stream(tweaks)
                .anyMatch(tweak -> ArrayUtils.contains(improvementKeys, tweak.improvement))
                || super.isTweakable(itemStack);
    }

    @Override
    public TweakData[] getTweaks(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        String variant = tag.getString(this.variantTagKey);
        String[] improvementKeys = Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.key)
                .toArray(String[]::new);
        return Arrays.stream(tweaks)
                .filter(tweak -> variant.equals(tweak.variant) || ArrayUtils.contains(improvementKeys, tweak.improvement))
                .toArray(TweakData[]::new);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack) {
        ItemStack[] salvage = super.removeModule(targetStack);

        CompoundNBT tag = NBTHelper.getTag(targetStack);
        Arrays.stream(improvements)
            .map(improvement -> slotTagKey + ":" + improvement.key)
            .forEach(tag::remove);

        clearProgression(targetStack);

        return salvage;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.attributes)
                .filter(Objects::nonNull)
                .reduce(super.getAttributeModifiers(itemStack), AttributeHelper::merge);
    }

    @Override
    public ItemProperties getProperties(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .reduce(super.getProperties(itemStack), ItemProperties::merge, ItemProperties::merge);
    }

    @Override
    public EffectData getEffectData(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.effects)
                .filter(Objects::nonNull)
                .reduce(super.getEffectData(itemStack), EffectData::merge);
    }

    @Override
    public ToolData getToolData(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.tools)
                .filter(Objects::nonNull)
                .reduce(super.getToolData(itemStack), ToolData::merge);
    }

    @Override
    public int getMagicCapacityGain(ItemStack itemStack) {
        return super.getMagicCapacityGain(itemStack) + getImprovementMagicCapacityGain(itemStack);
    }

    @Override
    public int getMagicCapacityCost(ItemStack itemStack) {
        return super.getMagicCapacityCost(itemStack) + getImprovementMagicCapacityCost(itemStack);
    }

    private int getImprovementMagicCapacityGain(ItemStack itemStack) {
        return Math.round(ConfigHandler.magicCapacityMultiplier.get().floatValue() *
                Arrays.stream(getImprovements(itemStack))
                        .mapToInt(improvement -> improvement.magicCapacity)
                        .filter(magicCapacity -> magicCapacity > 0)
                        .sum());
    }

    private int getImprovementMagicCapacityCost(ItemStack itemStack) {
        return -Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.magicCapacity)
                .filter(integrity -> integrity < 0)
                .sum();
    }

    protected ModuleModel[] getImprovementModels(ItemStack itemStack, int tint) {
        return Arrays.stream(getImprovements(itemStack))
                .filter(improvement -> improvement.models.length > 0)
                .flatMap(improvement -> Arrays.stream(improvement.models))
                .map(model -> ItemColors.inherit == model.tint ? new ModuleModel(model.type, model.location, tint) : model)
                .toArray(ModuleModel[]::new);
    }

    @Override
    public ModuleModel[] getModels(ItemStack itemStack) {
        ModuleModel[] models = super.getModels(itemStack);
        return ArrayUtils.addAll(models, getImprovementModels(itemStack, models.length > 0 ? models[0].tint : 0xffffff));
    }
}
