package se.mickelus.tetra.items.modular;

import com.google.common.cache.Cache;
import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.client.override.ImprovementOverride;
import se.mickelus.tetra.effect.*;
import se.mickelus.tetra.effect.data.DataEffectsHandler;
import se.mickelus.tetra.effect.vexing.VexingEffect;
import se.mickelus.tetra.event.ModularItemDamageEvent;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.schematic.RepairDefinition;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.mickelus.tetra.util.ItemStackTagHelper.*;

public interface IModularItem {
    Logger logger = LogManager.getLogger();

    GuiModuleOffsets[] defaultMajorOffsets = {
            new GuiModuleOffsets(),
            new GuiModuleOffsets(4, 0),
            new GuiModuleOffsets(4, 0, 4, 18),
            new GuiModuleOffsets(4, 0, 4, 18, -14, 0),
            new GuiModuleOffsets(4, 0, 4, 18, -14, 0, -14, 18)
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

    static void updateIdentifier(ItemStack itemStack) {
        mutate(itemStack, IModularItem::updateIdentifier);
        ModularItemComponentHelper.sync(itemStack);
    }

    static void updateIdentifier(CompoundTag nbt) {
        nbt.putString(identifierKey, UUID.randomUUID().toString());
    }

    /**
     * Helper for manually adding modules, to be used in cases like creative tab items which are populated before modules exists. Use
     * with caution as this may break things if the module/variant doesn't actually end up existing.
     *
     * @param itemStack
     * @param slot
     * @param module
     * @param moduleVariantKey
     * @param moduleVariant
     */
    static void putModuleInSlot(ItemStack itemStack, String slot, String module, String moduleVariantKey, String moduleVariant) {
        mutate(itemStack, tag -> {
            tag.putString(slot, module);
            tag.putString(moduleVariantKey, moduleVariant);
        });
    }

    static void putModuleInSlot(ItemStack itemStack, String slot, String module, String moduleVariant) {
        mutate(itemStack, tag -> {
            tag.putString(slot, module);
            tag.putString(module + "_material", moduleVariant);
        });
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

    static boolean isHoneable(ItemStack itemStack) {
        return Optional.ofNullable(getTag(itemStack))
                .map(tag -> tag.contains(honeAvailableKey))
                .orElse(false);
    }

    static int getHoningSeed(ItemStack itemStack) {
        return Optional.ofNullable(getTag(itemStack))
                .map(tag -> tag.getInt(honeCountKey))
                .orElse(0);
    }

    static void removeHoneable(ItemStack itemStack) {
        if (!hasTag(itemStack)) {
            return;
        }
        mutate(itemStack, tag -> {
            tag.remove(honeAvailableKey);
            tag.remove(honeProgressKey);
            tag.putInt(honeCountKey, tag.getInt(honeCountKey) + 1);
        });
    }

    static String getImprovementName(String key, int level, ItemStack itemStack) {
        String name = null;
        if (ImprovementOverride.names.hasOverride(key)) {
            name = ImprovementOverride.names.resolve(key, level, itemStack);
        }

        if (name == null) {
            if (I18n.exists("tetra.improvement." + key + ".name")) {
                name = I18n.get("tetra.improvement." + key + ".name");
            } else {
                int lastSlash = key.lastIndexOf("/");
                if (lastSlash != -1) {
                    String templateKey = "tetra.improvement." + key.substring(0, lastSlash) + ".name";
                    if (I18n.exists(templateKey)) {
                        String materialKey = "tetra.material." + key.substring(lastSlash + 1) + ".prefix";
                        if (I18n.exists(materialKey)) {
                            name = StringUtils.capitalize(I18n.get(templateKey, I18n.get(materialKey).toLowerCase()));
                        }
                    }
                }

                if (name == null) {
                    name = "tetra.improvement." + key + ".name";
                }
            }

            if (level > 0) {
                name += " " + I18n.get("enchantment.level." + level);
            }
        }

        return name;
    }

    static String getImprovementDescription(String key, int level, @Nullable ItemStack itemStack) {
        if (ImprovementOverride.descriptions.hasOverride(key)) {
            String name = ImprovementOverride.descriptions.resolve(key, level, itemStack);
            if (name != null) {
                return name;
            }
        }

        if (I18n.exists("tetra.improvement." + key + ".description")) {
            return I18n.get("tetra.improvement." + key + ".description");
        }

        int lastSlash = key.lastIndexOf("/");
        if (lastSlash != -1) {
            String splitKey = "tetra.improvement." + key.substring(0, lastSlash) + ".description";
            if (I18n.exists(splitKey)) {
                return I18n.get(splitKey);
            }
        }

        return "tetra.improvement." + key + ".description";
    }

    static ItemStack removeAllEnchantments(ItemStack itemStack) {
        removeTagKey(itemStack, "Enchantments");
        removeTagKey(itemStack, "StoredEnchantments");
        Arrays.stream(((IModularItem) itemStack.getItem()).getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .forEach(module -> module.removeEnchantments(itemStack));

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    Item getItem();

    default ItemStack getDefaultStack() {
        return new ItemStack(getItem());
    }

    @Nullable
    default String getIdentifier(ItemStack itemStack) {
        if (hasTag(itemStack)) {
            return getTag(itemStack).getString(identifierKey);
        }

        return null;
    }

    default String getDataCacheKey(ItemStack itemStack) {
        return Optional.ofNullable(getIdentifier(itemStack))
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> hasTag(itemStack) ? getTag(itemStack).toString() : "INVALID-" + getItem().toString());
    }

    default String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return Optional.ofNullable(getIdentifier(itemStack))
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> hasTag(itemStack) ? getTag(itemStack).toString() : "INVALID-" + getItem().toString());
    }

    void clearCaches();

    String[] getMajorModuleKeys(ItemStack itemStack);

    String[] getMinorModuleKeys(ItemStack itemStack);

    String[] getRequiredModules(ItemStack itemStack);

    default boolean isModuleRequired(ItemStack itemStack, String moduleSlot) {
        return ArrayUtils.contains(getRequiredModules(itemStack), moduleSlot);
    }

    default Collection<ItemModule> getAllModules(ItemStack stack) {
        CompoundTag stackTag = getTag(stack);

        if (stackTag != null) {
            return Stream.concat(Arrays.stream(getMajorModuleKeys(stack)), Arrays.stream(getMinorModuleKeys(stack)))
                    .map(stackTag::getString)
                    .map(ItemUpgradeRegistry.instance::getModule)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    default ItemModuleMajor[] getMajorModules(ItemStack itemStack) {
        String[] majorModuleKeys = getMajorModuleKeys(itemStack);
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        CompoundTag tag = getTag(itemStack);

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
        String[] minorModuleKeys = getMinorModuleKeys(itemStack);
        ItemModule[] modules = new ItemModule[minorModuleKeys.length];
        CompoundTag tag = getTag(itemStack);

        if (tag != null) {
            for (int i = 0; i < minorModuleKeys.length; i++) {
                String moduleName = tag.getString(minorModuleKeys[i]);
                ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
                modules[i] = module;
            }
        }
        return modules;
    }

    default int getNumMajorModules(ItemStack itemStack) {
        return getMajorModuleKeys(itemStack).length;
    }

    default String[] getMajorModuleNames(ItemStack itemStack) {
        return Arrays.stream(getMajorModuleKeys(itemStack))
                .map(key -> I18n.get("tetra.slot." + key))
                .toArray(String[]::new);
    }

    default int getNumMinorModules(ItemStack itemStack) {
        return getMinorModuleKeys(itemStack).length;
    }

    default String[] getMinorModuleNames(ItemStack itemStack) {
        return Arrays.stream(getMinorModuleKeys(itemStack))
                .map(key -> I18n.get("tetra.slot." + key))
                .toArray(String[]::new);
    }

    default boolean hasModule(ItemStack itemStack, ItemModule module) {
        return getAllModules(itemStack).stream()
                .anyMatch(module::equals);
    }

    default ItemModule getModuleFromSlot(ItemStack itemStack, String slot) {
        return Optional.ofNullable(getTag(itemStack))
                .map(tag -> tag.getString(slot))
                .map(ItemUpgradeRegistry.instance::getModule)
                .orElse(null);
    }

    default void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (!ConfigHandler.moduleProgression.get()) {
            return;
        }

        tickHoningProgression(entity, itemStack, multiplier);
        
        Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .forEach(module -> module.tickProgression(entity, itemStack, multiplier));
    }

    default void tickHoningProgression(@Nullable LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (!ConfigHandler.moduleProgression.get() || !canGainHoneProgress(itemStack)) {
            return;
        }

        if (isHoneable(itemStack)) {
            return;
        }

        // todo: store this in a separate data structure?
        mutate(itemStack, tag -> {
            int honingProgress = tag.contains(honeProgressKey) ? tag.getInt(honeProgressKey) : getHoningLimit(itemStack);
            honingProgress -= multiplier;
            tag.putInt(honeProgressKey, honingProgress);

            if (honingProgress <= 0 && !tag.getBoolean(honeAvailableKey)) {
                tag.putBoolean(honeAvailableKey, true);

                if (entity instanceof ServerPlayer serverPlayer) {
                    TetraMod.packetHandler.sendTo(new HonePacket(itemStack), serverPlayer);
                }
            }
        });
    }

    default int getHoningProgress(ItemStack itemStack) {
        return Optional.ofNullable(getTag(itemStack))
                .filter(tag -> tag.contains(honeProgressKey))
                .map(tag -> tag.getInt(honeProgressKey))
                .orElseGet(() -> getHoningLimit(itemStack));
    }

    default void setHoningProgress(ItemStack itemStack, int progress) {
        mutate(itemStack, tag -> {
            tag.putInt(honeProgressKey, progress);
            if (progress <= 0) {
                tag.putBoolean(honeAvailableKey, true);
            } else {
                tag.remove(honeAvailableKey);
            }
        });
    }

    default int getHoningLimit(ItemStack itemStack) {
        float workableFactor = (100f - getEffectLevel(itemStack, ItemEffect.workable)) / 100;
        return (int) Math.max((getHoneBase(itemStack) + getHoneIntegrityMultiplier(itemStack) * getIntegrityCost(itemStack)) * workableFactor, 1);
    }

    int getHoneBase(ItemStack itemStack);

    int getHoneIntegrityMultiplier(ItemStack itemStack);

    default int getHoningIntegrityPenalty(ItemStack itemStack) {
        return getHoneIntegrityMultiplier(itemStack) * getIntegrityCost(itemStack);
    }

    default int getHonedCount(ItemStack itemStack) {
        return Optional.ofNullable(getTag(itemStack))
                .map(tag -> tag.getInt(honeCountKey))
                .orElse(0);
    }

    boolean canGainHoneProgress(ItemStack itemStack);

    /**
     * Applies usage effects and ticks progression based on the given multiplier, should typically be called when the item is used
     * for something.
     *
     * @param entity     The using entity
     * @param itemStack  The used itemstack
     * @param multiplier A multiplier representing the effort and effect yielded from the use
     */
    default void applyUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        ApplyUsageEffectsEvent event = new ApplyUsageEffectsEvent(entity, itemStack, multiplier);
        NeoForge.EVENT_BUS.post(event);

        DataEffectsHandler.applyOnUseEffects(itemStack, entity);

        applyPositiveUsageEffects(entity, itemStack, event.getPositiveMultiplier());
        applyNegativeUsageEffects(entity, itemStack, event.getNegativeMultiplier());
    }

    default void applyPositiveUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        tickProgression(entity, itemStack, (int) multiplier);
    }

    default void applyNegativeUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        VexingEffect.perform(entity, itemStack, multiplier);
        FierySelfEffect.perform(entity, itemStack, multiplier);
        EnderReverbEffect.perform(entity, itemStack, multiplier);
        CombustingEffect.perform(entity, itemStack, multiplier);
        RavenousEffect.perform(entity, itemStack, multiplier);
    }

    /**
     * Helper method to be called in damageItem in implementing classes. Implement durability damage effects in here. When damaging items from
     * effects,
     * call {@link #applyDamage} instead.
     */
    default <T extends LivingEntity> int damageItemImpl(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        ModularItemDamageEvent event = new ModularItemDamageEvent(entity, stack, amount);
        NeoForge.EVENT_BUS.post(event);
        amount = event.getAmount();

        amount = BloodboundEffect.reduceDamage(stack, entity, amount);

        return Math.min(stack.getMaxDamage() - stack.getDamageValue() - 1, amount);
    }

    /**
     * Helper for damaging the item from an affect, durability effects should go in {@link #damageItemImpl}.
     */
    default void applyDamage(int amount, ItemStack itemStack, @Nullable LivingEntity responsibleEntity) {
        int damage = itemStack.getDamageValue();
        int maxDamage = itemStack.getMaxDamage();

        if (!isBroken(damage, maxDamage)) {
            int reducedAmount = getReducedDamage(amount, itemStack, responsibleEntity);
            if (responsibleEntity != null) {
                InteractionHand usedHand = responsibleEntity.getUsedItemHand();
                EquipmentSlot slot = usedHand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                itemStack.hurtAndBreak(reducedAmount, responsibleEntity, slot);
            } else {
                itemStack.setDamageValue(Math.min(itemStack.getDamageValue() + reducedAmount, itemStack.getMaxDamage()));
            }

            if (isBroken(damage + reducedAmount, maxDamage) && responsibleEntity != null && !responsibleEntity.level().isClientSide) {
                responsibleEntity.playSound(SoundEvents.SHIELD_BREAK, 1, 1);
            }
        }
    }

    /**
     * Emulates vanilla damage reductions, used when damaging items through tetra effects.
     */
    private int getReducedDamage(int amount, ItemStack itemStack, @Nullable LivingEntity responsibleEntity) {
        if (amount > 0) {
            int level = getEffectLevel(itemStack, ItemEffect.unbreaking);
            int reduction = 0;
            RandomSource random = responsibleEntity != null ? responsibleEntity.level().random : RandomSource.create();

            if (level > 0) {
                for (int i = 0; i < amount; i++) {
                    if (shouldIgnoreDurabilityDrop(itemStack, level, random)) {
                        reduction++;
                    }
                }
            }

            return amount - reduction;
        }
        return amount;
    }

    /**
     * Mirrors the old Unbreaking chance formula that Forge exposed via DigDurabilityEnchantment.
     */
    private boolean shouldIgnoreDurabilityDrop(ItemStack itemStack, int level, RandomSource random) {
        if (itemStack.getItem() instanceof ArmorItem && random.nextFloat() < 0.6F) {
            return false;
        }

        return random.nextInt(level + 1) > 0;
    }

    default boolean isBroken(ItemStack itemStack) {
        return isBroken(itemStack.getDamageValue(), itemStack.getMaxDamage());
    }

    default boolean isBroken(int damage, int maxDamage) {
        return maxDamage != 0 && damage >= maxDamage - 1;
    }

    @OnlyIn(Dist.CLIENT)
    default List<Component> getTooltip(ItemStack itemStack, @Nullable Level world, TooltipFlag advanced) {
        List<Component> tooltip = Lists.newArrayList();
        if (isBroken(itemStack)) {
            tooltip.add(Component.translatable("item.tetra.modular.broken")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .forEach(module -> {

                        tooltip.add(Component.literal("\u00BB ").withStyle(ChatFormatting.DARK_GRAY)
                                .append(Component.literal(module.getName(itemStack)).withStyle(ChatFormatting.GRAY)));

                        module.getEnchantmentHolders(itemStack).forEach((enchantment, level) ->
                                tooltip.add(Component.literal("  - " + TetraEnchantmentHelper.getEnchantmentName(enchantment, level))
                                        .withStyle(ChatFormatting.DARK_GRAY)));

                        Arrays.stream(module.getImprovements(itemStack))
                                .map(improvement -> "  - " + getImprovementTooltip(improvement.key, improvement.level, true))
                                .map(Component::literal)
                                .map(textComponent -> textComponent.withStyle(ChatFormatting.DARK_GRAY))
                                .forEach(tooltip::add);
                    });
            Arrays.stream(getMinorModules(itemStack))
                    .filter(Objects::nonNull)
                    .map(module -> Component.literal(" * ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(module.getName(itemStack)).withStyle(ChatFormatting.GRAY)))
                    .forEach(tooltip::add);

            // honing tooltip
            if (ConfigHandler.moduleProgression.get() && canGainHoneProgress(itemStack)) {
                if (isHoneable(itemStack)) {
                    tooltip.add(Component.literal(" > ").withStyle(ChatFormatting.AQUA)
                            .append(Component.translatable("tetra.hone.available").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY))));
                } else {
                    int progress = getHoningProgress(itemStack);
                    int base = getHoningLimit(itemStack);
                    String percentage = String.format("%.0f", 100f * (base - progress) / base);
                    tooltip.add(Component.literal(" > ").withStyle(ChatFormatting.DARK_AQUA)
                            .append(Component.translatable("tetra.hone.progress", base - progress, base, percentage).withStyle(ChatFormatting.GRAY)));
                }
            }
        } else {
            itemStack.getTagEnchantments().entrySet().stream()
                    .map(entry -> net.minecraft.world.item.enchantment.Enchantment.getFullname(entry.getKey(), entry.getIntValue()))
                    .forEach(tooltip::add);

            tooltip.add(Tooltips.expand);
        }

        return tooltip;
    }

    default String getImprovementTooltip(String key, int level, boolean clearFormatting) {
        if (clearFormatting) {
            return ChatFormatting.stripFormatting(getImprovementName(key, level, null));
        }

        return getImprovementName(key, level, null);
    }

//    default RepairInstance[] getRepairInstances(ItemStack itemStack) {
//        List<RepairInstance> instances = getAllModules(itemStack).stream()
//                .map(itemModule -> new RepairInstance(itemModule.getRepairDefinitions(itemStack), itemModule))
//                .toList();
//
//        fire event here
//    }

    /**
     * Returns an optional with the module that will be repaired in next repair attempt, the optional is empty if
     * there are no repairable modules in this item.
     *
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
     *
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
     *
     * @param itemStack     The itemstack for the modular item
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
     *
     * @param itemStack The itemstack for the modular item
     * @return
     */
    default int getRepairAmount(ItemStack itemStack) {
        return getItem().getMaxDamage(itemStack);
    }

    default Collection<ItemAbility> getRepairRequiredTools(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairRequiredTools(itemStack, materialStack))
                .orElseGet(Collections::emptySet);
    }

    default Map<ItemAbility, Integer> getRepairRequiredToolLevels(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairRequiredToolLevels(itemStack, materialStack))
                .orElseGet(Collections::emptyMap);
    }

    default int getRepairRequiredToolLevel(ItemStack itemStack, ItemStack materialStack, ItemAbility toolAction) {
        return getRepairModule(itemStack)
                .filter(module -> module.getRepairRequiredTools(itemStack, materialStack).contains(toolAction))
                .map(module -> module.getRepairRequiredToolLevel(itemStack, materialStack, toolAction))
                .map(level -> Math.max(1, level))
                .orElse(0);
    }

    default int getRepairRequiredExperience(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairExperienceCost(itemStack, materialStack))
                .orElse(0);
    }

    /**
     * Returns the number of times this item has been repaired.
     *
     * @param itemStack The itemstack for the modular item
     * @return
     */
    default int getRepairCount(ItemStack itemStack) {
        return Optional.ofNullable(getTag(itemStack))
                .map(tag -> tag.getInt(repairCountKey))
                .orElse(0);
    }

    default void incrementRepairCount(ItemStack itemStack) {
        mutate(itemStack, tag -> tag.putInt(repairCountKey, tag.getInt(repairCountKey) + 1));
    }

    default void repair(ItemStack itemStack) {
        getItem().setDamage(itemStack, getItem().getDamage(itemStack) - getRepairAmount(itemStack));

        incrementRepairCount(itemStack);
    }

    /**
     * Stability modifier for magic capacity, the stabilizing and unstable effects should increase/decrease the magic capacity of all modules by a
     * percentage equal to the effect levels
     *
     * @param itemStack
     * @return
     */
    default float getStabilityModifier(ItemStack itemStack) {
        return 1 + (getEffectLevel(itemStack, ItemEffect.stabilizing) - getEffectLevel(itemStack, ItemEffect.unstable)) / 100f;
    }

    default void tweak(ItemStack itemStack, String slot, Map<String, Integer> tweaks) {
        ItemModule module = getModuleFromSlot(itemStack, slot);
        double durabilityFactor = 0;

        if (module == null || !module.isTweakable(itemStack)) {
            return;
        }

        if (itemStack.isDamageableItem()) {
            durabilityFactor = itemStack.getDamageValue() * 1d / itemStack.getMaxDamage();
        }

        tweaks.forEach((tweakKey, step) -> {
            if (module.hasTweak(itemStack, tweakKey)) {
                module.setTweakStep(itemStack, tweakKey, step);
            }
        });

        if (itemStack.isDamageableItem()) {
            itemStack.setDamageValue((int) Math.floor((durabilityFactor * itemStack.getMaxDamage())));
        }

        updateIdentifier(itemStack);
    }

    /**
     * Returns attribute modifiers gained from item effects, e.g. attack speed from the counterweight
     *
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
                .map(this::fixIdentifiers)
                .orElse(null);
    }

    default Multimap<Attribute, AttributeModifier> fixIdentifiers(Multimap<Attribute, AttributeModifier> modifiers) {
        return AttributeHelper.fixIdentifiers(modifiers);
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

    default Set<TagKey<Item>> getTags(ItemStack itemStack) {
        return getPropertiesCached(itemStack).tags;
    }

    default int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        if (isBroken(itemStack)) {
            return -1;
        }

        return getEffectDataCached(itemStack).getLevel(effect);
    }

    default float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
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
                                .map(improvement -> Pair.of(improvement.prefixPriority, "tetra.improvement." + improvement.key + ".prefix"))
                                .filter(pair -> I18n.exists(pair.getSecond()))
                                .map(pair -> Pair.of(pair.getFirst(), I18n.get(pair.getSecond()))),
                        getAllModules(itemStack).stream()
                                .map(module -> Pair.of(module.getItemPrefixPriority(itemStack), module.getItemPrefix(itemStack)))
                                .filter(pair -> pair.getSecond() != null)
                )
                .sorted(Comparator.<Pair<Priority, String>, Priority>comparing(Pair::getFirst).reversed())
                .limit(2)
                .map(Pair::getSecond)
                .reduce("", (result, prefix) -> result + prefix + " ");
    }

    default String getItemName(ItemStack itemStack) {
        // todo: since getItemStackDisplayName is called on the server we cannot use the I18n service
        if (FMLEnvironment.dist.isDedicatedServer()) {
            return "";
        }

        String name = Arrays.stream(getSynergyData(itemStack))
                .map(synergyData -> synergyData.name)
                .filter(Objects::nonNull)
                .map(key -> "tetra.synergy." + key)
                .filter(I18n::exists)
                .map(I18n::get)
                .findFirst()
                .orElse(null);

        if (name == null) {
            name = getAllModules(itemStack).stream()
                    .sorted(Comparator.<ItemModule, Priority>comparing(module -> module.getItemNamePriority(itemStack)).reversed())
                    .map(module -> module.getItemName(itemStack))
                    .filter(Objects::nonNull)
                    .findFirst().orElse("");
        }

        String prefixes = getDisplayNamePrefixes(itemStack);
        return WordUtils.capitalize(prefixes + name);
    }

    SynergyData[] getAllSynergyData(ItemStack itemStack);

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
                    .filter(synergy -> synergy.modules.length == 0 || hasModuleSynergy(itemStack, synergy, modules))
                    .filter(synergy -> synergy.moduleVariants.length == 0 || hasVariantSynergy(synergy, variantKeys))
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
     *
     * @param itemStack The modular item itemstack
     * @param severity
     */
    default void assemble(ItemStack itemStack, @Nullable Level world, float severity) {
        if (itemStack.getDamageValue() > itemStack.getMaxDamage()) {
            itemStack.setDamageValue(itemStack.getMaxDamage());
        }

        // this stops the tooltip renderer from showing enchantments
        mutate(itemStack, nbt -> nbt.putInt("HideFlags", 1));

        updateIdentifier(itemStack);
    }

    default boolean acceptsEnchantment(ItemStack itemStack, Enchantment enchantment, boolean fromTable) {
        return Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .anyMatch(module -> module.acceptsEnchantment(itemStack, enchantment, fromTable));
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
    default ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .map(itemModule -> itemModule.getModels(itemStack))
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(IModuleModel::getRenderLayer))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    @OnlyIn(Dist.CLIENT)
    default String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    default GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return defaultMajorOffsets[getNumMajorModules(itemStack)];
    }

    @OnlyIn(Dist.CLIENT)
    default GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return defaultMinorOffsets[getNumMinorModules(itemStack)];
    }
}
