package se.mickelus.tetra.module;


import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.*;
import java.util.stream.Collectors;

import static se.mickelus.tetra.util.ItemStackTagHelper.*;

public abstract class ItemModuleMajor extends ItemModule {

    public static final String settleImprovement = "settled";
    public static final String arrestedImprovement = "arrested";
    protected ImprovementData[] improvements = new ImprovementData[0];
    protected int settleMax = 0;
    private String settleProgressKey = "/settle_progress";

    public ItemModuleMajor(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        settleProgressKey = getSlot() + settleProgressKey;
    }

    public static void addImprovement(ItemStack itemStack, String slot, String improvement, int level) {
        IModularItem item = (IModularItem) itemStack.getItem();
        CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                .filter(module -> module.acceptsImprovementLevel(improvement, level))
                .ifPresent(module -> module.addImprovement(itemStack, improvement, level));
    }

    public static void removeImprovement(ItemStack itemStack, String slot, String improvement) {
        if (hasTag(itemStack)) {
            mutate(itemStack, tag -> tag.remove(slot + ":" + improvement));
        }
    }

    public void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        int settleMaxCount = getSettleMaxCount(itemStack);
        if (settleMaxCount == 0) {
            return;
        }

        int settleLevel = getImprovementLevel(itemStack, settleImprovement);

        if (settleLevel < settleMaxCount && (getImprovementLevel(itemStack, arrestedImprovement) == -1)) {
            int settleProgress = getSettleProgress(itemStack) - multiplier;

            mutate(itemStack, tag -> {
                tag.putInt(settleProgressKey, settleProgress);
                if (settleProgress <= 0) {
                    addImprovement(itemStack, settleImprovement, settleLevel == -1 ? 1 : settleLevel + 1);
                    tag.remove(settleProgressKey);

                    if (entity instanceof ServerPlayer) {
                        TetraMod.packetHandler.sendTo(new SettlePacket(itemStack, getSlot()), (ServerPlayer) entity);
                        IModularItem.updateIdentifier(tag);
                    }
                }
            });
        }
    }

    /**
     * Returns the remaining number of times the item has to be used before this module will settle.
     *
     * @param itemStack The itemstack which the module is present on
     * @return
     */
    public int getSettleProgress(ItemStack itemStack) {
        return Optional.ofNullable(getTag(itemStack))
                .filter(tag -> tag.contains(settleProgressKey))
                .map(tag -> tag.getInt(settleProgressKey))
                .orElseGet(() -> getSettleLimit(itemStack));
    }

    /**
     * Returns the total number of times the item has to be used before this module will settle.
     *
     * @param itemStack The itemstack which the module is present on
     * @return
     */
    public int getSettleLimit(ItemStack itemStack) {
        return (int) ((ConfigHandler.settleLimitBase.get() + getDurability(itemStack) * ConfigHandler.settleLimitDurabilityMultiplier.get())
                * Math.max(getImprovementLevel(itemStack, settleImprovement) * ConfigHandler.settleLimitLevelMultiplier.get(), 1f));
    }

    /**
     * Returns the total number of times the item has to be used before this module will settle.
     *
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
        if (hasTag(itemStack)) {
            mutate(itemStack, tag -> tag.remove(settleProgressKey));
        }
    }

    public int getImprovementLevel(ItemStack itemStack, String improvementKey) {
        return Optional.ofNullable(getTag(itemStack))
                .filter(tag -> tag.contains(slotTagKey + ":" + improvementKey))
                .map(tag -> tag.getInt(slotTagKey + ":" + improvementKey))
                .orElse(-1);
    }

    public ImprovementData getImprovement(ItemStack itemStack, String improvementKey) {
        if (hasTag(itemStack)) {
            CompoundTag tag = getTag(itemStack);
            return Arrays.stream(improvements)
                    .filter(improvement -> improvementKey.equals(improvement.key))
                    .filter(improvement -> tag.contains(slotTagKey + ":" + improvement.key))
                    .filter(improvement -> improvement.level == tag.getInt(slotTagKey + ":" + improvement.key))
                    .findAny()
                    .orElse(null);
        }

        return null;
    }

    public ImprovementData[] getImprovements(ItemStack itemStack) {
        if (hasTag(itemStack)) {
            CompoundTag tag = getTag(itemStack);
            return Arrays.stream(improvements)
                    .filter(improvement -> tag.contains(slotTagKey + ":" + improvement.key))
                    .filter(improvement -> improvement.level == tag.getInt(slotTagKey + ":" + improvement.key))
                    .toArray(ImprovementData[]::new);
        }

        return new ImprovementData[0];
    }

    public String[] getAcceptedImprovements() {
        return Arrays.stream(improvements)
                .map(improvement -> improvement.key)
                .distinct()
                .toArray(String[]::new);
    }


    public String[] getAcceptedImprovements(ItemAspect aspect) {
        return Arrays.stream(improvements)
                .filter(improvement -> improvement.aspects != null && improvement.aspects.contains(aspect))
                .map(improvement -> improvement.key)
                .distinct()
                .toArray(String[]::new);
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
        mutate(itemStack, tag -> tag.putInt(slotTagKey + ":" + improvementKey, level));
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

    public void removeEnchantments(ItemStack itemStack) {
        TetraEnchantmentHelper.removeEnchantments(itemStack, getSlot());
    }

    public boolean acceptsEnchantment(ItemStack itemStack, Holder<Enchantment> enchantment, boolean fromTable) {
        return Optional.ofNullable(getAspects(itemStack))
                .map(AspectData::getLevelMap)
                .filter(aspects -> TetraEnchantmentHelper.isApplicableForAspects(enchantment, fromTable, aspects))
                .isPresent();
    }

    public Set<String> getEnchantmentKeys(ItemStack itemStack) {
        if (itemStack.getTagEnchantments().isEmpty()) {
            return Collections.emptySet();
        }

        TetraEnchantmentHelper.ensureMappings(itemStack);
        CompoundTag mappings = getTagElement(itemStack, "EnchantmentMapping");
        if (mappings != null) {
            return mappings.getAllKeys().stream()
                    .filter(key -> getSlot().equals(mappings.get(key).getAsString()))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public Map<String, Integer> getEnchantmentsPrimitive(ItemStack itemStack) {
        if (itemStack.getTagEnchantments().isEmpty()) {
            return Collections.emptyMap();
        }

        TetraEnchantmentHelper.ensureMappings(itemStack);
        CompoundTag mappings = getTagElement(itemStack, "EnchantmentMapping");

        if (mappings != null) {
            return itemStack.getTagEnchantments().entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey().unwrapKey().orElseThrow().location().toString(), entry.getIntValue()))
                    .filter(entry -> getSlot().equals(mappings.getString(entry.getLeft())))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        }

        return Collections.emptyMap();
    }

    public Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        return getEnchantmentHolders(itemStack).entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().value(), Map.Entry::getValue, Integer::max, LinkedHashMap::new));
    }

    public Map<Holder<Enchantment>, Integer> getEnchantmentHolders(ItemStack itemStack) {
        if (itemStack.getTagEnchantments().isEmpty()) {
            return Collections.emptyMap();
        }

        TetraEnchantmentHelper.ensureMappings(itemStack);
        CompoundTag mappings = getTagElement(itemStack, "EnchantmentMapping");

        if (mappings != null) {
            return itemStack.getTagEnchantments().entrySet().stream()
                    .filter(entry -> TetraEnchantmentHelper.getEnchantmentKey(entry.getKey())
                            .map(Object::toString)
                            .map(key -> getSlot().equals(mappings.getString(key)))
                            .orElse(false))
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getIntValue(), Integer::max, LinkedHashMap::new));
        }

        return Collections.emptyMap();
    }

    public int getEnchantmentMagicCapacityCost(ItemStack itemStack) {
        return -getEnchantments(itemStack).entrySet().stream()
                .mapToInt(entry -> TetraEnchantmentHelper.getEnchantmentCapacityCost(entry.getKey(), entry.getValue()))
                .sum();
    }

    public int getAvailableMagicCapacityForMapping(ItemStack itemStack) {
        return getMagicCapacityGain(itemStack) - (super.getMagicCapacityCost(itemStack) + getImprovementMagicCapacityCost(itemStack));
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
        if (hasTag(itemStack)) {
            String variant = getTag(itemStack).getString(this.variantTagKey);
            String[] improvementKeys = Arrays.stream(getImprovements(itemStack))
                    .map(improvement -> improvement.key)
                    .toArray(String[]::new);

            return Arrays.stream(tweaks)
                    .filter(tweak -> variant.equals(tweak.variant) || ArrayUtils.contains(improvementKeys, tweak.improvement))
                    .toArray(TweakData[]::new);
        }

        return new TweakData[0];
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack, boolean upgrade) {
        ItemStack[] salvage = super.removeModule(targetStack, upgrade);

        if (!upgrade && hasTag(targetStack)) {
            mutate(targetStack, tag -> Arrays.stream(improvements)
                    .map(improvement -> slotTagKey + ":" + improvement.key)
                    .forEach(tag::remove));

            clearProgression(targetStack);
        }

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

    public AspectData getAspects(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.aspects)
                .filter(Objects::nonNull)
                .reduce(super.getAspects(itemStack), AspectData::merge);
    }

    @Override
    public int getMagicCapacityGain(ItemStack itemStack) {
        return super.getMagicCapacityGain(itemStack) + getImprovementMagicCapacityGain(itemStack);
    }

    @Override
    public int getMagicCapacityCost(ItemStack itemStack) {
        return super.getMagicCapacityCost(itemStack) + getImprovementMagicCapacityCost(itemStack) + getEnchantmentMagicCapacityCost(itemStack);
    }

    public int getImprovementMagicCapacityGain(ItemStack itemStack) {
        return Math.round(ConfigHandler.magicCapacityMultiplier.get().floatValue()
                * CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getStabilityModifier(itemStack))
                .orElse(1f)
                * Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.magicCapacity)
                .filter(magicCapacity -> magicCapacity > 0)
                .sum());
    }

    public int getImprovementMagicCapacityCost(ItemStack itemStack) {
        return -Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.magicCapacity)
                .filter(integrity -> integrity < 0)
                .sum();
    }

    protected IModuleModel[] getImprovementModels(ItemStack itemStack, SimpleColor tint) {
        return Arrays.stream(getImprovements(itemStack))
                .filter(improvement -> improvement.models.length > 0)
                .flatMap(improvement -> Arrays.stream(improvement.models))
                .map(model -> model.inheritTint(tint))
                .toArray(IModuleModel[]::new);
    }

    @Override
    public IModuleModel[] getModels(ItemStack itemStack) {
        IModuleModel[] models = super.getModels(itemStack);
        return ArrayUtils.addAll(models, getImprovementModels(itemStack, models.length > 0 ? models[0].getOverlayTint() : new SimpleColor(0xffffff)));
    }
}
