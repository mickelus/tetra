package se.mickelus.tetra.items.modular;

import com.google.common.cache.Cache;
import com.google.common.collect.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.forgespi.Environment;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.effect.EnderReverbEffect;
import se.mickelus.tetra.effect.FierySelfEffect;
import se.mickelus.tetra.effect.HauntedEffect;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.schematic.RepairDefinition;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IModularItem {
    Logger logger = LogManager.getLogger();

    GuiModuleOffsets[] defaultMajorOffsets = {
            new GuiModuleOffsets(),
            new GuiModuleOffsets(4, 0),
            new GuiModuleOffsets(4, 0, 4, 18),
            new GuiModuleOffsets(4, 0, 4, 18, -4, 0),
            new GuiModuleOffsets(4, 0, 4, 18, -4, 0, -4, 18)
    };

    GuiModuleOffsets[] defaultMinorOffsets = {
            new GuiModuleOffsets(),
            new GuiModuleOffsets(-21, 12),
            new GuiModuleOffsets(-18, 5, -18, 18),
            new GuiModuleOffsets(-12, -1, -21, 12, -12, 25),
    };

    String identifierKey = "id";

    String repairCountKey = "repairCount";

    String cooledStrengthKey = "cooledStrength";

    String honeProgressKey = "honing_progress";
    String honeAvailableKey = "honing_available";
    String honeCountKey = "honing_count";

    public Item getItem();

    default ItemStack getDefaultStack() {
        return new ItemStack(getItem());
    }

    static void updateIdentifier(ItemStack itemStack) {
        updateIdentifier(itemStack.getOrCreateTag());
    }

    static void updateIdentifier(CompoundNBT nbt) {
        nbt.putString(identifierKey, UUID.randomUUID().toString());
    }

    @Nullable
    default String getIdentifier(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            return itemStack.getTag().getString(identifierKey);
        }

        return null;
    }

    default String getDataCacheKey(ItemStack itemStack) {
        return Optional.ofNullable(getIdentifier(itemStack))
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> itemStack.hasTag() ? itemStack.getTag().toString() : "INVALID-" + getItem().getRegistryName());
    }

    default String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return Optional.ofNullable(getIdentifier(itemStack))
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> itemStack.hasTag() ? itemStack.getTag().toString() : "INVALID-" + getItem().getRegistryName());
    }

    void clearCaches();

    public String[] getMajorModuleKeys();
    public String[] getMinorModuleKeys();
    public String[] getRequiredModules();

    default boolean isModuleRequired(String moduleSlot) {
        return ArrayUtils.contains(getRequiredModules(), moduleSlot);
    }

    default Collection<ItemModule> getAllModules(ItemStack stack) {
        CompoundNBT stackTag = stack.getTag();

        if (stackTag != null) {
            return Stream.concat(Arrays.stream(getMajorModuleKeys()),Arrays.stream(getMinorModuleKeys()))
                    .map(stackTag::getString)
                    .map(ItemUpgradeRegistry.instance::getModule)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    default ItemModuleMajor[] getMajorModules(ItemStack itemStack) {
        String[] majorModuleKeys = getMajorModuleKeys();
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        CompoundNBT tag = itemStack.getTag();

        if (tag != null) {
            for (int i = 0; i < majorModuleKeys.length; i++) {
                String moduleName = tag.getString(majorModuleKeys[i]);
                ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
                if (module instanceof ItemModuleMajor) {
                    modules[i] = (ItemModuleMajor) module;
                }
            }
        }
        return modules;
    }

    default ItemModule[] getMinorModules(ItemStack itemStack) {
        String[] minorModuleKeys = getMinorModuleKeys();
        ItemModule[] modules = new ItemModule[minorModuleKeys.length];
        CompoundNBT tag = itemStack.getTag();

        if (tag != null) {
            for (int i = 0; i < minorModuleKeys.length; i++) {
                String moduleName = tag.getString(minorModuleKeys[i]);
                ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
                modules[i] = module;
            }
        }
        return modules;
    }

    default int getNumMajorModules() {
        return getMajorModuleKeys().length;
    }

    default String[] getMajorModuleNames() {
        return Arrays.stream(getMajorModuleKeys())
                .map(key -> I18n.format("tetra.slot." + key))
                .toArray(String[]::new);
    }

    default int getNumMinorModules() {
        return getMinorModuleKeys().length;
    }

    default String[] getMinorModuleNames() {
        return Arrays.stream(getMinorModuleKeys())
                .map(key -> I18n.format("tetra.slot." + key))
                .toArray(String[]::new);
    }

    /**
     * Helper for manually adding modules, to be used in cases like creative tab items which are populated before modules exists. Use
     * with caution as this may break things if the module/variant doesn't actually end up existing.
     * @param itemStack
     * @param slot
     * @param module
     * @param moduleVariantKey
     * @param moduleVariant
     */
    static void putModuleInSlot(ItemStack itemStack, String slot, String module, String moduleVariantKey, String moduleVariant) {
        CompoundNBT tag = itemStack.getOrCreateTag();
        tag.putString(slot, module);
        tag.putString(moduleVariantKey, moduleVariant);
    }

    static void putModuleInSlot(ItemStack itemStack, String slot, String module, String moduleVariant) {
        CompoundNBT tag = itemStack.getOrCreateTag();
        tag.putString(slot, module);
        tag.putString(module + "_material", moduleVariant);
    }

    default boolean hasModule(ItemStack itemStack, ItemModule module) {
        return getAllModules(itemStack).stream()
                .anyMatch(module::equals);
    }

    default ItemModule getModuleFromSlot(ItemStack itemStack, String slot) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getString(slot))
                .map(ItemUpgradeRegistry.instance::getModule)
                .orElse(null);
    }

    static int getIntegrityGain(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getPropertiesCached(itemStack))
                .map(properties -> properties.integrity)
                .orElse(0);
    }

    static int getIntegrityCost(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getPropertiesCached(itemStack))
                .map(properties -> properties.integrityUsage)
                .orElse(0);
    }

    default void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (!ConfigHandler.moduleProgression.get()) {
            return;
        }

        tickHoningProgression(entity, itemStack, multiplier);

        for (ItemModuleMajor module: getMajorModules(itemStack)) {
            module.tickProgression(entity, itemStack, multiplier);
        }
    }

    default void tickHoningProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (!ConfigHandler.moduleProgression.get() || !canGainHoneProgress()) {
            return;
        }

        // todo: store this in a separate data structure?
        CompoundNBT tag = itemStack.getOrCreateTag();
        if (!isHoneable(itemStack)) {
            int honingProgress;
            if (tag.contains(honeProgressKey)) {
                honingProgress = tag.getInt(honeProgressKey);
            } else {
                honingProgress = getHoningLimit(itemStack);
            }

            honingProgress -= multiplier;
            tag.putInt(honeProgressKey, honingProgress);

            if (honingProgress <= 0 && !isHoneable(itemStack)) {
                tag.putBoolean(honeAvailableKey, true);

                if (entity instanceof ServerPlayerEntity) {
                    TetraMod.packetHandler.sendTo(new HonePacket(itemStack), (ServerPlayerEntity) entity);
                }
            }
        }

    }

    default int getHoningProgress(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getTag())
                .filter(tag -> tag.contains(honeProgressKey))
                .map(tag -> tag.getInt(honeProgressKey))
                .orElseGet(() -> getHoningLimit(itemStack));
    }

    default void setHoningProgress(ItemStack itemStack, int progress) {
        itemStack.getOrCreateTag().putInt(honeProgressKey, progress);
    }

    default int getHoningLimit(ItemStack itemStack) {
        float workableFactor = (100f - getEffectLevel(itemStack, ItemEffect.workable)) / 100;
        return (int) Math.max((getHoneBase() + getHoneIntegrityMultiplier() * getIntegrityCost(itemStack)) * workableFactor, 1);
    }

    int getHoneBase();

    int getHoneIntegrityMultiplier();

    default int getHoningIntegrityPenalty(ItemStack itemStack) {
        return getHoneIntegrityMultiplier() * getIntegrityCost(itemStack);
    }

    default int getHonedCount(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getInt(honeCountKey))
                .orElse(0);
    }

    boolean canGainHoneProgress();

    static boolean isHoneable(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.contains(honeAvailableKey))
                .orElse(false);
    }

    static int getHoningSeed(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getInt(honeCountKey))
                .orElse(0);
    }

    static void removeHoneable(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();

        if (tag != null) {
            tag.remove(honeAvailableKey);
            tag.remove(honeProgressKey);
            tag.putInt(honeCountKey, tag.getInt(honeCountKey) + 1);
        }
    }

    /**
     * Applies usage effects and ticks progression based on the given multiplier, should typically be called when the item is used
     * for something.
     *
     * @param entity The using entity
     * @param itemStack The used itemstack
     * @param multiplier A multiplier representing the effort and effect yielded from the use
     */
    default void applyUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        applyPositiveUsageEffects(entity, itemStack, multiplier);
        applyNegativeUsageEffects(entity, itemStack, multiplier);
    }

    default void applyPositiveUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        tickProgression(entity, itemStack, (int) multiplier);
    }

    default void applyNegativeUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        HauntedEffect.perform(entity, itemStack, multiplier);
        FierySelfEffect.perform(entity, itemStack, multiplier);
        EnderReverbEffect.perform(entity, itemStack, multiplier);
    }

    default void applyDamage(int amount, ItemStack itemStack, LivingEntity responsibleEntity) {
        int damage = itemStack.getDamage();
        int maxDamage = itemStack.getMaxDamage();

        if (!isBroken(damage, maxDamage)) {
            int reducedAmount = getReducedDamage(amount, itemStack, responsibleEntity);
            itemStack.damageItem(reducedAmount, responsibleEntity, breaker -> breaker.sendBreakAnimation(breaker.getActiveHand()));

            if (isBroken(damage + reducedAmount, maxDamage) && !responsibleEntity.world.isRemote) {
                responsibleEntity.sendBreakAnimation(responsibleEntity.getActiveHand());
                responsibleEntity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 1, 1);
            }
        }
    }

    default int getReducedDamage(int amount, ItemStack itemStack, LivingEntity responsibleEntity) {
        if (amount > 0) {
            int level = getEffectLevel(itemStack, ItemEffect.unbreaking);
            int reduction = 0;

            if (level > 0) {
                for (int i = 0; i < amount; i++) {
                    if (UnbreakingEnchantment.negateDamage(itemStack, level, responsibleEntity.world.rand)) {
                        reduction++;
                    }
                }
            }

            return amount - reduction;
        }
        return amount;
    }

    default boolean isBroken(ItemStack itemStack) {
        return isBroken(itemStack.getDamage(), itemStack.getMaxDamage());
    }

    default boolean isBroken(int damage, int maxDamage) {
        return maxDamage != 0 && damage >= maxDamage - 1;
    }

    @OnlyIn(Dist.CLIENT)
    default List<ITextComponent> getTooltip(ItemStack itemStack, @Nullable World world, ITooltipFlag advanced) {
        List<ITextComponent> tooltip = Lists.newArrayList();
        if (isBroken(itemStack)) {
            tooltip.add(new TranslationTextComponent("item.tetra.modular.broken")
                    .mergeStyle(TextFormatting.DARK_RED, TextFormatting.ITALIC));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .forEach(module -> {
                        tooltip.add(new StringTextComponent("\u00BB ").mergeStyle(TextFormatting.DARK_GRAY)
                                .append(new StringTextComponent(module.getName(itemStack)).mergeStyle(TextFormatting.GRAY)));
                        Arrays.stream(module.getImprovements(itemStack))
                                .map(improvement -> String.format("  - %s", getImprovementTooltip(improvement.key, improvement.level, true)))
                                .map(StringTextComponent::new)
                                .map(textComponent -> textComponent.mergeStyle(TextFormatting.DARK_GRAY))
                                .forEach(tooltip::add);
                    });
            Arrays.stream(getMinorModules(itemStack))
                    .filter(Objects::nonNull)
                    .map(module -> new StringTextComponent(" * ").mergeStyle(TextFormatting.DARK_GRAY)
                            .append(new StringTextComponent(module.getName(itemStack)).mergeStyle(TextFormatting.GRAY)))
                    .forEach(tooltip::add);

            // honing tooltip
            if (ConfigHandler.moduleProgression.get() && canGainHoneProgress()) {
                if (isHoneable(itemStack)) {
                    tooltip.add(new StringTextComponent(" > ").mergeStyle(TextFormatting.AQUA)
                            .append(new TranslationTextComponent("tetra.hone.available").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY))));
                } else {
                    int progress = getHoningProgress(itemStack);
                    int base = getHoningLimit(itemStack);
                    String percentage = String.format("%.0f", 100f * (base - progress) / base);
                    tooltip.add(new StringTextComponent(" > ").mergeStyle(TextFormatting.DARK_AQUA)
                            .append(new TranslationTextComponent("tetra.hone.progress", base - progress, base, percentage).mergeStyle(TextFormatting.GRAY)));
                }
            }
        } else {
            Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .flatMap(module -> Arrays.stream(module.getImprovements(itemStack)))
                    .filter(improvement -> improvement.enchantment)
                    .collect(Collectors.groupingBy(ImprovementData::getKey, Collectors.summingInt(ImprovementData::getLevel)))
                    .entrySet()
                    .stream()
                    .map(entry -> getImprovementTooltip(entry.getKey(), entry.getValue(), false))
                    .map(StringTextComponent::new)
                    .map(text -> text.mergeStyle(TextFormatting.GRAY))
                    .forEach(tooltip::add);

            tooltip.add(Tooltips.expand);
        }

        return tooltip;
    }

    default String getImprovementTooltip(String key, int level, boolean clearFormatting) {
        String tooltip = I18n.format("tetra.improvement." + key + ".name");
        if (level > 0) {
            tooltip += " " + I18n.format("enchantment.level." + level);
        }

        if (clearFormatting) {
            return TextFormatting.getTextWithoutFormattingCodes(tooltip);
        }
        return tooltip;
    }

    /**
     * Returns an optional with the module that will be repaired in next repair attempt, the optional is empty if
     * there are no repairable modules in this item.
     * @param itemStack The itemstack for the modular item
     * @return An optional with the module that will be repaired in next repair attempt
     */
    default Optional<ItemModule> getRepairModule(ItemStack itemStack) {
        List<ItemModule> modules = getAllModules(itemStack).stream()
                .filter(itemModule -> !itemModule.getRepairDefinitions(itemStack).isEmpty())
                .collect(Collectors.toList());

        if (modules.size() > 0) {
            int repairCount = getRepairCount(itemStack);
            return Optional.of(modules.get(repairCount % modules.size()));
        }
        return Optional.empty();
    }

    default ItemModule[] getRepairCycle(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .filter(module -> !module.getRepairDefinitions(itemStack).isEmpty())
                .toArray(ItemModule[]::new);
    }

    default String getRepairModuleName(ItemStack itemStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getName(itemStack))
                .orElse(null);
    }

    default String getRepairSlot(ItemStack itemStack) {
        return getRepairModule(itemStack)
                .map(ItemModule::getSlot)
                .orElse(null);
    }

    /**
     * Returns a collection of definitions for all possible ways to perform the next repair attempt. Rotates between materials required
     * for different modules
     * @param itemStack The itemstack for the modular item
     * @return a collection of definitions, empty if none are available
     */
    default Collection<RepairDefinition> getRepairDefinitions(ItemStack itemStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairDefinitions(itemStack))
                .orElse(null);
    }

    /**
     * Returns the required size of the repair material itemstack for the next repair attempt.
     * @param itemStack The itemstack for the modular item
     * @param materialStack The material stack that is to be used to repair the item
     * @return
     */
    default int getRepairMaterialCount(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairDefinition(itemStack, materialStack))
                .map(definition -> definition.material.count)
                .orElse(0);
    }

    /**
     * Returns the amount of durability restored by the next repair attempt.
     * @param itemStack The itemstack for the modular item
     * @return
     */
    default int getRepairAmount(ItemStack itemStack) {
        return getItem().getMaxDamage(itemStack);
    }

    default Collection<ToolType> getRepairRequiredTools(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairRequiredTools(itemStack, materialStack))
                .orElseGet(Collections::emptySet);
    }

    default Map<ToolType, Integer> getRepairRequiredToolLevels(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairRequiredToolLevels(itemStack, materialStack))
                .orElseGet(Collections::emptyMap);
    }

    default int getRepairRequiredToolLevel(ItemStack itemStack, ItemStack materialStack, ToolType toolType) {
        return getRepairModule(itemStack)
                .filter(module -> module.getRepairRequiredTools(itemStack, materialStack).contains(toolType))
                .map(module -> module.getRepairRequiredToolLevel(itemStack, materialStack, toolType))
                .map(level -> Math.max(1, level))
                .orElse(0);
    }

    default int getRepairRequiredExperience(ItemStack itemStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairExperienceCost(itemStack))
                .orElse(0);
    }

    /**
     * Returns the number of times this item has been repaired.
     * @param itemStack The itemstack for the modular item
     * @return
     */
    default int getRepairCount(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getInt(repairCountKey))
                .orElse(0);
    }

    default void incrementRepairCount(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getOrCreateTag();
        tag.putInt(repairCountKey, tag.getInt(repairCountKey) + 1);
    }

    default void repair(ItemStack itemStack) {
        getItem().setDamage(itemStack, getItem().getDamage(itemStack) - getRepairAmount(itemStack));

        incrementRepairCount(itemStack);
    }

    default Map<Enchantment, Integer> getEnchantmentsFromImprovements(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> Arrays.stream(item.getMajorModules(itemStack)))
                .orElseGet(Stream::empty)
                .filter(Objects::nonNull)
                .flatMap(module -> Arrays.stream(module.getImprovements(itemStack)))
                .forEach(improvement -> {
                    for (EnchantmentMapping mapping : ItemUpgradeRegistry.instance.getEnchantmentMappings(improvement.key)) {
                        enchantments.merge(mapping.enchantment, (int) (improvement.level * mapping.multiplier), Integer::sum);
                    }
                });

        return enchantments;
    }

    default int getEnchantmentLevelFromImprovements(ItemStack itemStack, Enchantment enchantment) {
        return Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .flatMap(module -> Arrays.stream(module.getImprovements(itemStack)))
                .mapToInt(improvement ->
                        (int) (Math.max(1, improvement.level) * Arrays.stream(ItemUpgradeRegistry.instance.getEnchantmentMappings(improvement.key))
                                .filter(mapping -> enchantment.equals(mapping.enchantment))
                                .map(mapping -> mapping.multiplier)
                                .reduce(0f, Float::sum))
                )
                .sum();
    }

    default int getEnchantmentLevelFromImprovements(ItemStack itemStack, String slot, Enchantment enchantment) {
        return CastOptional.cast(getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                .map(module -> Arrays.stream(module.getImprovements(itemStack)))
                .orElseGet(Stream::empty)
                .mapToInt(improvement ->
                        (int) (Math.max(1, improvement.level) * Arrays.stream(ItemUpgradeRegistry.instance.getEnchantmentMappings(improvement.key))
                                .filter(mapping -> enchantment.equals(mapping.enchantment))
                                .map(mapping -> mapping.multiplier)
                                .reduce(0f, Float::sum))
                )
                .sum();
    }

    default int getEnchantmentLevelFromImprovements(ItemStack itemStack, String slot, String improvementKey, Enchantment enchantment) {
        return CastOptional.cast(getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                .map(module -> Arrays.stream(module.getImprovements(itemStack)))
                .orElseGet(Stream::empty)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .mapToInt(improvement ->
                        (int) (Math.max(1, improvement.level) * Arrays.stream(ItemUpgradeRegistry.instance.getEnchantmentMappings(improvement.key))
                                .filter(mapping -> enchantment.equals(mapping.enchantment))
                                .map(mapping -> mapping.multiplier)
                                .reduce(0f, Float::sum))
                )
                .sum();
    }

    /**
     * Stability modifier for magic capacity, the stabilizing and unstable effects should increase/decrease the magic capacity of all modules by a
     * percentage equal to the effect levels
     * @param itemStack
     * @return
     */
    default float getStabilityModifier(ItemStack itemStack) {
        return 1 + (getEffectLevel(itemStack, ItemEffect.stabilizing) - getEffectLevel(itemStack, ItemEffect.unstable)) / 100f;
    }

    default void applyDestabilizationEffects(ItemStack itemStack, World world, float probabilityMultiplier) {
        if (!world.isRemote) {
            Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .forEach(module -> {
                        int instability = -module.getMagicCapacity(itemStack);

                        if (instability > 0) {
                            float destabilizationChance = module.getDestabilizationChance(itemStack, probabilityMultiplier);
                            DestabilizationEffect[] possibleEffects =
                                    DestabilizationEffect.getEffectsForImprovement(instability, module.getImprovements(itemStack));

                            do {
                                if (destabilizationChance > world.rand.nextFloat()) {
                                    DestabilizationEffect effect = possibleEffects[world.rand.nextInt(possibleEffects.length)];
                                    int currentEffectLevel = module.getImprovementLevel(itemStack, effect.destabilizationKey);
                                    int newLevel;

                                    if (currentEffectLevel >= 0) {
                                        newLevel = currentEffectLevel + 1;
                                    } else if (effect.minLevel == effect.maxLevel) {
                                        newLevel = effect.minLevel;
                                    } else {
                                        newLevel = effect.minLevel + world.rand.nextInt(effect.maxLevel - effect.minLevel);
                                    }

                                    if (module.acceptsImprovementLevel(effect.destabilizationKey, newLevel)) {
                                        module.addImprovement(itemStack, effect.destabilizationKey, newLevel);
                                    }
                                }

                                destabilizationChance--;
                            } while (destabilizationChance > 1);
                        }

                    });
        }
    }

    default void tweak(ItemStack itemStack, String slot, Map<String, Integer> tweaks) {
        ItemModule module = getModuleFromSlot(itemStack, slot);
        double durabilityFactor = 0;

        if (module == null || !module.isTweakable(itemStack)) {
            return;
        }

        if (itemStack.isDamageable()) {
            durabilityFactor = itemStack.getDamage() * 1d / itemStack.getMaxDamage();
        }

        tweaks.forEach((tweakKey, step) -> {
            if (module.hasTweak(itemStack, tweakKey)) {
                module.setTweakStep(itemStack, tweakKey, step);
            }
        });

        if (itemStack.isDamageable()) {
            itemStack.setDamage((int) Math.ceil((durabilityFactor * itemStack.getMaxDamage()
                    - (durabilityFactor * durabilityFactor * module.getDurability(itemStack)))));
        }

        updateIdentifier(itemStack);
    }

    /**
     * Returns attribute modifiers gained from item effects, e.g. attack speed from the counterweight
     * @param itemStack
     * @return
     */
    default Multimap<Attribute, AttributeModifier> getEffectAttributes(ItemStack itemStack) {
        return AttributeHelper.emptyMap;
    }


    default Multimap<Attribute, AttributeModifier> getModuleAttributes(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getAttributeModifiers(itemStack))
                .filter(Objects::nonNull)
                .reduce(null, AttributeHelper::merge);
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> attributes = AttributeHelper.merge(
                getModuleAttributes(itemStack),
                getEffectAttributes(itemStack));

        return Arrays.stream(getSynergyData(itemStack))
                .map(synergy -> synergy.attributes)
                .filter(Objects::nonNull)
                .reduce(attributes, AttributeHelper::merge);
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiersCollapsed(ItemStack itemStack) {
        if (logger.isDebugEnabled()) {
            logger.debug("Gathering attribute modifiers for {} ({})", getItemName(itemStack), getDataCacheKey(itemStack));
        }
        return Optional.ofNullable(getAttributeModifiers(itemStack))
                .map(modifiers -> modifiers
                        .asMap()
                        .entrySet()
                        .stream()
                        .collect(Multimaps.flatteningToMultimap(
                                Map.Entry::getKey,
                                entry -> AttributeHelper.collapse(entry.getValue()).stream(),
                                ArrayListMultimap::create)))
                .map(AttributeHelper::fixIdentifiers)
                .orElse(null);
    }

    Cache<String, Multimap<Attribute, AttributeModifier>> getAttributeModifierCache();

    default Multimap<Attribute, AttributeModifier> getAttributeModifiersCached(ItemStack itemStack) {
        try {
            return getAttributeModifierCache().get(getDataCacheKey(itemStack),
                    () -> Optional.ofNullable(getAttributeModifiersCollapsed(itemStack)).orElseGet(ImmutableMultimap::of));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return getAttributeModifiersCollapsed(itemStack);
        }
    }

    default double getAttributeValue(ItemStack itemStack, Attribute attribute) {
        if (isBroken(itemStack)) {
            return 0;
        }

        return AttributeHelper.getMergedAmount(getAttributeModifiersCached(itemStack).get(attribute));
    }

    default double getAttributeValue(ItemStack itemStack, Attribute attribute, double base) {
        if (isBroken(itemStack)) {
            return 0;
        }

        return AttributeHelper.getMergedAmount(getAttributeModifiersCached(itemStack).get(attribute), base);
    }

    default EffectData getEffectData(ItemStack itemStack) {
        if (logger.isDebugEnabled()) {
            logger.debug("Gathering effect data for {} ({})", getItemName(itemStack), getDataCacheKey(itemStack));
        }
        return Stream.concat(
                getAllModules(itemStack).stream()
                        .map(module -> module.getEffectData(itemStack)),
                Arrays.stream(getSynergyData(itemStack))
                        .map(synergy -> synergy.effects))
                .filter(Objects::nonNull)
                .reduce(null, EffectData::merge);
    }

    Cache<String, EffectData> getEffectDataCache();

    default EffectData getEffectDataCached(ItemStack itemStack) {
        try {
            return getEffectDataCache().get(getDataCacheKey(itemStack),
                    () -> Optional.ofNullable(getEffectData(itemStack)).orElseGet(EffectData::new));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Optional.ofNullable(getEffectData(itemStack)).orElseGet(EffectData::new);
        }
    }

    default ItemProperties getProperties(ItemStack itemStack) {
        if (logger.isDebugEnabled()) {
            logger.debug("Gathering properties for {} ({})", getItemName(itemStack), getDataCacheKey(itemStack));
        }

        return Stream.concat(
                getAllModules(itemStack).stream().map(module -> module.getProperties(itemStack)),
                Arrays.stream(getSynergyData(itemStack)))
                .reduce(new ItemProperties(), ItemProperties::merge);
    }

    Cache<String, ItemProperties> getPropertyCache();

    default ItemProperties getPropertiesCached(ItemStack itemStack) {
        try {
            return getPropertyCache().get(getDataCacheKey(itemStack), () -> getProperties(itemStack));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return getProperties(itemStack);
        }
    }

    default int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        if (isBroken(itemStack)) {
            return -1;
        }

        return getEffectDataCached(itemStack).getLevel(effect);
    }

    default double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        if (isBroken(itemStack)) {
            return 0;
        }

        return getEffectDataCached(itemStack).getEfficiency(effect);
    }

    default Collection<ItemEffect> getEffects(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return Collections.emptyList();
        }

        return getEffectDataCached(itemStack).getValues();
    }

    default ImprovementData[] getImprovements(ItemStack itemStack) {
        return Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .flatMap(module -> Arrays.stream(module.getImprovements(itemStack)))
                .toArray(ImprovementData[]::new);
    }

    default String getDisplayNamePrefixes(ItemStack itemStack) {
        return Stream.concat(
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> improvement.key + ".prefix")
                        .filter(I18n::hasKey)
                        .map(I18n::format),
                getAllModules(itemStack).stream()
                        .sorted(Comparator.comparing(module -> module.getItemPrefixPriority(itemStack)))
                        .map(module -> module.getItemPrefix(itemStack))
                        .filter(Objects::nonNull)
        )
                .limit(2)
                .reduce("", (result, prefix) -> result + prefix + " ");
    }

    default String getItemName(ItemStack itemStack) {
        // todo: since getItemStackDisplayName is called on the server we cannot use the I18n service
        if (Environment.get().getDist().isDedicatedServer()) {
            return "";
        }

        String name = Arrays.stream(getSynergyData(itemStack))
                .map(synergyData -> synergyData.name)
                .filter(Objects::nonNull)
                .map(key -> "tetra.synergy." + key)
                .filter(I18n::hasKey)
                .map(I18n::format)
                .findFirst()
                .orElse(null);

        if (name == null) {
            name = getAllModules(itemStack).stream()
                    .sorted(Comparator.comparing(module -> module.getItemNamePriority(itemStack)))
                    .map(module -> module.getItemName(itemStack))
                    .filter(Objects::nonNull)
                    .findFirst().orElse("");
        }

        String prefixes = getDisplayNamePrefixes(itemStack);
        return StringUtils.capitalize(prefixes + name);
    }

    public SynergyData[] getAllSynergyData(ItemStack itemStack);

    default SynergyData[] getSynergyData(ItemStack itemStack) {
        SynergyData[] synergies = getAllSynergyData(itemStack);
        if (synergies.length > 0) {
            ItemModule[] modules = getAllModules(itemStack).stream()
                    .sorted(Comparator.comparing(ItemModule::getUnlocalizedName))
                    .toArray(ItemModule[]::new);

            String[] variantKeys = getAllModules(itemStack).stream()
                    .map(module -> module.getVariantData(itemStack))
                    .map(data -> data.key)
                    .sorted()
                    .toArray(String[]::new);

            String[] improvements = Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .map(module -> module.getImprovements(itemStack))
                    .flatMap(Arrays::stream)
                    .map(data -> data.key)
                    .sorted()
                    .toArray(String[]::new);

            return Arrays.stream(synergies)
                    .filter(synergy -> hasVariantSynergy(synergy, variantKeys) || hasModuleSynergy(itemStack, synergy, modules))
                    .filter(synergy -> synergy.improvements.length == 0 || hasImprovementSynergy(synergy, improvements))
                    .toArray(SynergyData[]::new);
        }
        return new SynergyData[0];
    }

    default boolean hasImprovementSynergy(SynergyData synergy, String[] improvements) {
        int improvementMatches = 0;
        for (String improvement : improvements) {
            if (improvementMatches == synergy.improvements.length) {
                break;
            }

            if (improvement.equals(synergy.improvements[improvementMatches])) {
                improvementMatches++;
            }
        }

        return synergy.improvements.length > 0 && improvementMatches == synergy.improvements.length;
    }

    default boolean hasVariantSynergy(SynergyData synergy, String[] variantKeys) {
        int variantMatches = 0;
        for (String variantKey : variantKeys) {
            if (variantMatches == synergy.moduleVariants.length) {
                break;
            }

            if (variantKey.equals(synergy.moduleVariants[variantMatches])) {
                variantMatches++;
            }
        }

        return synergy.moduleVariants.length > 0 && variantMatches == synergy.moduleVariants.length;
    }

    default boolean hasModuleSynergy(ItemStack itemStack, SynergyData synergy, ItemModule[] modules) {
        int moduleMatches = 0;
        String variant = null;

        if (synergy.sameVariant) {
            for (ItemModule module : modules) {
                if (moduleMatches == synergy.modules.length) {
                    break;
                }

                String moduleKey = synergy.matchSuffixed ? module.getKey() : module.getUnlocalizedName();
                if (moduleKey.equals(synergy.modules[moduleMatches])) {
                    if (variant == null) {
                        variant = module.getVariantData(itemStack).key;
                    }

                    if (variant.equals(module.getVariantData(itemStack).key)) {
                        moduleMatches++;
                    }
                }
            }
        } else {
            for (ItemModule module : modules) {
                if (moduleMatches == synergy.modules.length) {
                    break;
                }

                String moduleKey = synergy.matchSuffixed ? module.getKey() : module.getUnlocalizedName();
                if (moduleKey.equals(synergy.modules[moduleMatches])) {
                    moduleMatches++;
                }
            }
        }

        return synergy.modules.length > 0 && moduleMatches == synergy.modules.length;
    }

    /**
     * Resets and applies effects for the current setup of modules & improvements. Applies enchantments and other things which cannot be emulated
     * through other means. Call this after each time the module setup changes.
     * @param itemStack The modular item itemstack
     * @param severity
     */
    default void assemble(ItemStack itemStack, @Nullable World world, float severity) {
        if (itemStack.getDamage() > itemStack.getMaxDamage()) {
            itemStack.setDamage(itemStack.getMaxDamage());
        }

        if (world != null) {
            applyDestabilizationEffects(itemStack, world, severity);
        }

        CompoundNBT nbt = itemStack.getOrCreateTag();

        // this stops the tooltip renderer from showing enchantments
        nbt.putInt("HideFlags", 1);

        EnchantmentHelper.setEnchantments(getEnchantmentsFromImprovements(itemStack), itemStack);

        updateIdentifier(itemStack);
    }

    default boolean hasEnchantments(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack)).anyMatch(improvement -> improvement.enchantment);
    }

    static ItemStack removeAllEnchantments(ItemStack itemStack) {
        itemStack.removeChildTag("Enchantments");
        itemStack.removeChildTag("StoredEnchantments");
        Arrays.stream(((IModularItem) itemStack.getItem()).getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .forEach(module -> module.removeEnchantments(itemStack));

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    default boolean canEnchantInEnchantingTable(ItemStack itemStack) {
        return getEnchantability(itemStack) > 0 && !hasEnchantments(itemStack);
    }

    default boolean acceptsEnchantment(ItemStack itemStack, Enchantment enchantment) {
        EnchantmentMapping[] mappings = ItemUpgradeRegistry.instance.getEnchantmentMappings(enchantment);
        if (mappings.length > 0) {
            return Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .anyMatch(module -> Arrays.stream(mappings).anyMatch(mapping -> module.acceptsImprovement(mapping.improvement)));
        }
        return false;
    }

    default int getEnchantability(ItemStack itemStack) {
        return (int) (Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .mapToInt(module -> module.getMagicCapacity(itemStack))
                .filter(capacity -> capacity > 0)
                .average()
                .orElse(0) / 6d);
    }

    @OnlyIn(Dist.CLIENT)
    default ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    @OnlyIn(Dist.CLIENT)
    default String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    default GuiModuleOffsets getMajorGuiOffsets() {
        return defaultMajorOffsets[getNumMajorModules()];
    }

    @OnlyIn(Dist.CLIENT)
    default GuiModuleOffsets getMinorGuiOffsets() {
        return defaultMinorOffsets[getNumMinorModules()];
    }
}
