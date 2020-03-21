package se.mickelus.tetra.module;


import com.google.common.collect.Streams;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.data.TweakData;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

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
        ItemModular item = (ItemModular) itemStack.getItem();
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
    public double getDamageModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToDouble(improvement -> improvement.damage)
                .sum() + super.getDamageModifier(itemStack);
    }

    @Override
    public double getDamageMultiplierModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.damageMultiplier)
                .reduce((float) super.getDamageMultiplierModifier(itemStack), (a, b) -> a * b);
    }

    @Override
    public double getSpeedModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToDouble(improvement -> improvement.attackSpeed)
                .sum() + super.getSpeedModifier(itemStack);
    }

    @Override
    public double getSpeedMultiplierModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.attackSpeedMultiplier)
                .reduce((float) super.getSpeedMultiplierModifier(itemStack), (a, b) -> a * b);
    }

    @Override
    public int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.effects)
                .mapToInt(effects -> effects.getLevel(effect))
                .sum() + super.getEffectLevel(itemStack, effect);
    }

    @Override
    public float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        return (float) Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.effects)
                .mapToDouble(effects -> effects.getEfficiency(effect))
                .sum() + super.getEffectEfficiency(itemStack, effect);
    }

    @Override
    public Collection<ItemEffect> getEffects(ItemStack itemStack) {
        return Streams.concat(
                super.getEffects(itemStack).stream(),
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> improvement.effects)
                        .flatMap(effects -> effects.getValues().stream()))
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvementData -> improvementData.capabilities)
                .mapToInt(capabilityData -> capabilityData.getLevel(capability))
                .sum() + super.getCapabilityLevel(itemStack, capability);
    }

    @Override
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability) {
        return (float) Arrays.stream(getImprovements(itemStack))
                .map(improvementData -> improvementData.capabilities)
                .mapToDouble(capabilityData -> capabilityData.getEfficiency(capability))
                .sum() + super.getCapabilityEfficiency(itemStack, capability);
    }

    @Override
    public Collection<Capability> getCapabilities(ItemStack itemStack) {
        return Streams.concat(
                super.getCapabilities(itemStack).stream(),
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> improvement.capabilities)
                        .flatMap(capabilities -> capabilities.getValues().stream()))
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public int getIntegrityGain(ItemStack itemStack) {
        return super.getIntegrityGain(itemStack) + getImprovementIntegrityGain(itemStack);
    }

    @Override
    public int getIntegrityCost(ItemStack itemStack) {
        return super.getIntegrityCost(itemStack) + getImprovementIntegrityCost(itemStack);
    }

    private int getImprovementIntegrityGain(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.integrity)
                .filter(integrity -> integrity > 0)
                .sum();
    }

    private int getImprovementIntegrityCost(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.integrity)
                .filter(integrity -> integrity < 0)
                .sum();
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

    @Override
    public int getDurability(ItemStack itemStack) {
        return (int)((super.getDurability(itemStack) + getImprovementDurability(itemStack)) * getImprovementDurabilityMultiplier(itemStack));
    }

    private int getImprovementDurability(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.durability)
                .sum();
    }

    private double getImprovementDurabilityMultiplier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToDouble(improvement -> improvement.durabilityMultiplier)
                .filter(integrity -> integrity > 0)
                .reduce(1, (a, b) -> a * b);
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
