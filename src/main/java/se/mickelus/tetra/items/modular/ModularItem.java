package se.mickelus.tetra.items.modular;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.forgespi.Environment;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.NBTHelper;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.schematic.RepairDefinition;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ModularItem extends TetraItem implements IItemModular, IToolProvider {
    private static final Logger logger = LogManager.getLogger();

    protected static final String identifierKey = "id";

    protected static final String repairCountKey = "repairCount";

    protected static final String cooledStrengthKey = "cooledStrength";

    private static final String honeProgressKey = "honing_progress";
    private static final String honeAvailableKey = "honing_available";
    private static final String honeCountKey = "honing_count";
    protected int honeBase = 450;
    protected int honeIntegrityMultiplier = 200;

    // static marker for item, denoting if it can progress towards being honed
    protected boolean canHone = true;

    protected String[] majorModuleKeys;
    protected String[] minorModuleKeys;

    protected String[] requiredModules = new String[0];

    protected int baseDurability = 0;
    protected int baseIntegrity = 0;

    protected SynergyData[] synergies = new SynergyData[0];

    public static final UUID attackDamageModifier = Item.ATTACK_DAMAGE_MODIFIER;
    public static final UUID attackSpeedModifier = Item.ATTACK_SPEED_MODIFIER;

    public static final float repairLevelFactor = 0.05f;
    public static final float cleanseLevelFactor = 0.1f;
    public static final float enchantLevelFactor = 0.1f;

    private Cache<String, Multimap<Attribute, AttributeModifier>> attributeCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, ToolData> toolCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, EffectData> effectCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, ItemProperties> propertyCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public ModularItem(Properties properties) {
        super(properties);

        DataManager.moduleData.onReload(this::clearCaches);
    }

    public String getDataCacheKey(ItemStack itemStack) {
        return Optional.of(getIdentifier(itemStack))
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> NBTHelper.getTag(itemStack).toString());
    }

    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return Optional.of(getIdentifier(itemStack))
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> NBTHelper.getTag(itemStack).toString());
    }

    public void clearCaches() {
        logger.debug("Clearing item data caches for {}...", getRegistryName());
        attributeCache.invalidateAll();
        toolCache.invalidateAll();
        effectCache.invalidateAll();
        propertyCache.invalidateAll();
    }

    @Override
    public int getMaxDamage(ItemStack itemStack) {
        return Optional.of(getPropertiesCached(itemStack))
                .map(properties -> (properties.durability + baseDurability) * properties.durabilityMultiplier)
                .map(Math::round)
                .orElse(0);
    }

    public static int getIntegrityGain(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getPropertiesCached(itemStack))
                .map(properties -> properties.integrity)
                .orElse(0);
    }

    public static int getIntegrityCost(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getPropertiesCached(itemStack))
                .map(properties -> properties.integrityUsage)
                .orElse(0);
    }

    protected Collection<ItemModule> getAllModules(ItemStack stack) {
        CompoundNBT stackTag = NBTHelper.getTag(stack);

        if (stackTag != null) {
            return Stream.concat(Arrays.stream(majorModuleKeys),Arrays.stream(minorModuleKeys))
                    .map(stackTag::getString)
                    .map(ItemUpgradeRegistry.instance::getModule)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack) {
        ItemModuleMajor[] modules = new ItemModuleMajor[majorModuleKeys.length];
        CompoundNBT stackTag = NBTHelper.getTag(itemStack);

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
        CompoundNBT stackTag = NBTHelper.getTag(itemStack);

        for (int i = 0; i < minorModuleKeys.length; i++) {
            String moduleName = stackTag.getString(minorModuleKeys[i]);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
            modules[i] = module;
        }

        return modules;
    }

    @Override
    public boolean isModuleRequired(String moduleSlot) {
        return ArrayUtils.contains(requiredModules, moduleSlot);
    }

    @Override
    public int getNumMajorModules() {
        return majorModuleKeys.length;
    }

    @Override
    public String[] getMajorModuleKeys() {
        return majorModuleKeys;
    }

    @Override
    public String[] getMajorModuleNames() {
        return Arrays.stream(majorModuleKeys)
                .map(key -> I18n.format("tetra.slot." + key))
                .toArray(String[]::new);
    }

    @Override
    public int getNumMinorModules() {
        return minorModuleKeys.length;
    }

    @Override
    public String[] getMinorModuleKeys() {
        return minorModuleKeys;
    }

    @Override
    public String[] getMinorModuleNames() {
        return Arrays.stream(minorModuleKeys)
                .map(key -> I18n.format("tetra.slot." + key))
                .toArray(String[]::new);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    public boolean hasModule(ItemStack itemStack, ItemModule module) {
        return getAllModules(itemStack).stream()
            .anyMatch(module::equals);
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
    public static void putModuleInSlot(ItemStack itemStack, String slot, String module, String moduleVariantKey, String moduleVariant) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        tag.putString(slot, module);
        tag.putString(moduleVariantKey, moduleVariant);
    }

    public static void putModuleInSlot(ItemStack itemStack, String slot, String module, String moduleVariant) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        tag.putString(slot, module);
        tag.putString(module + "_material", moduleVariant);
    }

    public ItemModule getModuleFromSlot(ItemStack itemStack, String slot) {
        return ItemUpgradeRegistry.instance.getModule(NBTHelper.getTag(itemStack).getString(slot));
    }

    public void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (!ConfigHandler.moduleProgression.get()) {
            return;
        }

        tickHoningProgression(entity, itemStack, multiplier);

        for (ItemModuleMajor module: getMajorModules(itemStack)) {
            module.tickProgression(entity, itemStack, multiplier);
        }
    }

    public void tickHoningProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (!ConfigHandler.moduleProgression.get() || !canHone) {
            return;
        }

        // todo: store this in a separate data structure?
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        if (!isHoneable(itemStack)) {
            int honingProgress;
            if (tag.contains(honeProgressKey)) {
                honingProgress = tag.getInt(honeProgressKey);
            } else {
                honingProgress = getHoningBase(itemStack);
            }

            honingProgress -= multiplier;
            tag.putInt(honeProgressKey, honingProgress);

            if (honingProgress <= 0 && !isHoneable(itemStack)) {
                tag.putBoolean(honeAvailableKey, true);

                if (entity instanceof ServerPlayerEntity) {
                    PacketHandler.sendTo(new HonePacket(itemStack), (ServerPlayerEntity) entity);
                }
            }
        }

    }

    public int getHoningProgress(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);

        if (tag.contains(honeProgressKey)) {
            return tag.getInt(honeProgressKey);
        }

        return getHoningBase(itemStack);
    }

    public void setHoningProgress(ItemStack itemStack, int progress) {
        NBTHelper.getTag(itemStack).putInt(honeProgressKey, progress);
    }

    public int getHoningBase(ItemStack itemStack) {
        float workableFactor = (100f - getEffectLevel(itemStack, ItemEffect.workable)) / 100;
        return (int) Math.max((honeBase + honeIntegrityMultiplier * getIntegrityCost(itemStack)) * workableFactor, 1);
    }

    public int getHonedCount(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getInt(honeCountKey);
    }

    public static boolean isHoneable(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).contains(honeAvailableKey);
    }

    public static int getHoningSeed(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getInt(honeCountKey) + 1;
    }

    public static void removeHoneable(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        tag.remove(honeAvailableKey);
        tag.remove(honeProgressKey);
        tag.putInt(honeCountKey, tag.getInt(honeCountKey) + 1);
    }

    protected void causeFierySelfEffect(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double fierySelfEfficiency = getEffectEfficiency(itemStack, ItemEffect.fierySelf);
            if (fierySelfEfficiency > 0) {
                BlockPos pos = entity.getPosition();
                float temperature = entity.world.getBiome(pos).getTemperature(pos);
                if (entity.getRNG().nextDouble() < fierySelfEfficiency * temperature * multiplier) {
                    entity.setFire(getEffectLevel(itemStack, ItemEffect.fierySelf));
                }
            }
        }
    }

    protected void causeEnderReverbEffect(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double effectProbability = getEffectEfficiency(itemStack, ItemEffect.enderReverb);
            if (effectProbability > 0
                    && !CastOptional.cast(entity, PlayerEntity.class).map(PlayerEntity::isCreative).orElse(false)
                    && entity.getRNG().nextDouble() < effectProbability * multiplier) {
                AxisAlignedBB aabb = new AxisAlignedBB(entity.getPosition()).grow(24);
                List<LivingEntity> nearbyTargets = entity.world.getEntitiesWithinAABB(LivingEntity.class, aabb,
                        target -> target instanceof EndermanEntity || target instanceof EndermiteEntity
                                || target instanceof ShulkerEntity || target instanceof EnderDragonEntity);
                if (nearbyTargets.size() > 0) {
                    nearbyTargets.get(entity.getRNG().nextInt(nearbyTargets.size())).setRevengeTarget(entity);
                }
            }
        }
    }

    protected void causeHauntEffect(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double effectProbability = getEffectEfficiency(itemStack, ItemEffect.haunted);
            if (effectProbability > 0) {
                if (entity.getRNG().nextDouble() < effectProbability * multiplier) {
                    int effectLevel = getEffectLevel(itemStack, ItemEffect.haunted);

                    VexEntity vex = EntityType.VEX.create(entity.world);
                    vex.setLimitedLife(effectLevel * 20);
                    vex.setLocationAndAngles(entity.getPosX(), entity.getPosY() + 1, entity.getPosZ(), entity.rotationYaw, 0.0F);
                    vex.setHeldItem(Hand.MAIN_HAND, itemStack.copy());
                    vex.setDropChance(EquipmentSlotType.MAINHAND, 0);
                    vex.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 2000 + effectLevel * 20));
                    entity.world.addEntity(vex);

                    // todo: use temporary modules for this instead once implemented
                    CastOptional.cast(itemStack.getItem(), ModularItem.class)
                            .map(item -> Arrays.stream(item.getMajorModules(itemStack)))
                            .orElse(Stream.empty())
                            .filter(Objects::nonNull)
                            .filter(module -> module.getImprovement(itemStack, ItemEffect.hauntedKey) != null)
                            .findAny()
                            .ifPresent(module -> {
                                int level = module.getImprovementLevel(itemStack, ItemEffect.hauntedKey);
                                if (level > 0) {
                                    module.addImprovement(itemStack, ItemEffect.hauntedKey, level - 1);
                                } else {
                                    module.removeImprovement(itemStack, ItemEffect.hauntedKey);
                                }
                            });

                    entity.world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_WITCH_AMBIENT, SoundCategory.PLAYERS, 2f, 2);
                }
            }
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
    public void applyUsageEffects(LivingEntity entity, ItemStack itemStack, double multiplier) {
        tickProgression(entity, itemStack, (int) multiplier);

        causeHauntEffect(entity, itemStack, multiplier);
        causeFierySelfEffect(entity, itemStack, multiplier);
        causeEnderReverbEffect(entity, itemStack, multiplier);
    }

    @Override
    public void setDamage(ItemStack itemStack, int damage) {
        super.setDamage(itemStack, Math.min(itemStack.getMaxDamage() - 1, damage));
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return Math.min(stack.getMaxDamage() - stack.getDamage() - 1, amount);
    }

    public void applyDamage(int amount, ItemStack itemStack, LivingEntity responsibleEntity) {
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

    private int getReducedDamage(int amount, ItemStack itemStack, LivingEntity responsibleEntity) {
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

    public boolean isBroken(ItemStack itemStack) {
        return isBroken(itemStack.getDamage(), itemStack.getMaxDamage());
    }

    private boolean isBroken(int damage, int maxDamage) {
        return maxDamage != 0 && damage >= maxDamage - 1;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
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
            if (ConfigHandler.moduleProgression.get() && canHone) {
                if (isHoneable(itemStack)) {
                    tooltip.add(new StringTextComponent(" > ").mergeStyle(TextFormatting.AQUA)
                            .append(new TranslationTextComponent("tetra.hone.available").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY))));
                } else {
                    int progress = getHoningProgress(itemStack);
                    int base = getHoningBase(itemStack);
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
    }

    private String getImprovementTooltip(String key, int level, boolean clearFormatting) {
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
    public Optional<ItemModule> getRepairModule(ItemStack itemStack) {
        List<ItemModule> modules = getAllModules(itemStack).stream()
                .filter(itemModule -> !itemModule.getRepairDefinitions(itemStack).isEmpty())
                .collect(Collectors.toList());

        if (modules.size() > 0) {
            int repairCount = getRepairCount(itemStack);
            return Optional.of(modules.get(repairCount % modules.size()));
        }
        return Optional.empty();
    }

    public ItemModule[] getRepairCycle(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .filter(module -> !module.getRepairDefinitions(itemStack).isEmpty())
                .toArray(ItemModule[]::new);
    }

    public String getRepairModuleName(ItemStack itemStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getName(itemStack))
                .orElse(null);
    }

    public String getRepairSlot(ItemStack itemStack) {
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
    public Collection<RepairDefinition> getRepairDefinitions(ItemStack itemStack) {
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
    public int getRepairMaterialCount(ItemStack itemStack, ItemStack materialStack) {
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
    public int getRepairAmount(ItemStack itemStack) {
        return getMaxDamage(itemStack);
    }

    public Collection<ToolType> getRepairRequiredTools(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairRequiredTools(itemStack, materialStack))
                .orElseGet(Collections::emptySet);
    }

    public Map<ToolType, Integer> getRepairRequiredToolLevels(ItemStack itemStack, ItemStack materialStack) {
        return getRepairModule(itemStack)
                .map(module -> module.getRepairRequiredToolLevels(itemStack, materialStack))
                .orElseGet(Collections::emptyMap);
    }

    public int getRepairRequiredToolLevel(ItemStack itemStack, ItemStack materialStack, ToolType toolType) {
        return getRepairModule(itemStack)
                .filter(module -> module.getRepairRequiredTools(itemStack, materialStack).contains(toolType))
                .map(module -> module.getRepairRequiredToolLevel(itemStack, materialStack, toolType))
                .map(level -> Math.max(1, level))
                .orElse(0);
    }

    public int getRepairRequiredExperience(ItemStack itemStack) {
        return getRepairModule(itemStack)
                .map(module -> -module.getMagicCapacity(itemStack))
                .map(capacity -> capacity * repairLevelFactor)
                .map(MathHelper::ceil)
                .map(capacity -> Math.max(0, capacity))
                .orElse(0);
    }

    /**
     * Returns the number of times this item has been repaired.
     * @param itemStack The itemstack for the modular item
     * @return
     */
    private int getRepairCount(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getInt(repairCountKey);
    }

    private void incrementRepairCount(ItemStack itemStack) {
        CompoundNBT tag = NBTHelper.getTag(itemStack);
        tag.putInt(repairCountKey, tag.getInt(repairCountKey) + 1);
    }

    public void repair(ItemStack itemStack) {
        setDamage(itemStack, getDamage(itemStack) - getRepairAmount(itemStack));

        incrementRepairCount(itemStack);
    }

    @Override
    public ItemStack getDefaultStack() {
        return new ItemStack(this);
    }

    public static void updateIdentifier(ItemStack itemStack) {
        NBTHelper.getTag(itemStack).putString(identifierKey, UUID.randomUUID().toString());
    }

    public String getIdentifier(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getString(identifierKey);
    }

    /**
     *
     * @param itemStack The modular item itemstack
     * @param world
     * @param severity Used by things like destabilization as multiplier for the probability of a destabilization to occur, a value of 0
     *                 would make it impossible for destabilization to occur.
     */
    @Override
    public void assemble(ItemStack itemStack, World world, float severity) {
        if (itemStack.getDamage() > itemStack.getMaxDamage()) {
            itemStack.setDamage(itemStack.getMaxDamage());
        }

        applyDestabilizationEffects(itemStack, world, severity);

        CompoundNBT nbt = NBTHelper.getTag(itemStack);

        // this stops the tooltip renderer from showing enchantments
        nbt.putInt("HideFlags", 1);

        EnchantmentHelper.setEnchantments(getEnchantmentsFromImprovements(itemStack), itemStack);

        updateIdentifier(itemStack);
    }

    @Override
    public void onCreated(ItemStack itemStack, World world, PlayerEntity player) {
        updateIdentifier(itemStack);
    }

    public Map<Enchantment, Integer> getEnchantmentsFromImprovements(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        CastOptional.cast(itemStack.getItem(), ModularItem.class)
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

    public int getEnchantmentLevelFromImprovements(ItemStack itemStack, Enchantment enchantment) {
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

    public int getEnchantmentLevelFromImprovements(ItemStack itemStack, String slot, Enchantment enchantment) {
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

    public int getEnchantmentLevelFromImprovements(ItemStack itemStack, String slot, String improvementKey, Enchantment enchantment) {
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
    public float getStabilityModifier(ItemStack itemStack) {
        return 1 + (getEffectLevel(itemStack, ItemEffect.stabilizing) - getEffectLevel(itemStack, ItemEffect.unstable)) / 100f;
    }

    private void applyDestabilizationEffects(ItemStack itemStack, World world, float probabilityMultiplier) {
        if (!world.isRemote) {
            Arrays.stream(getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .forEach(module -> {
                        int instability = -module.getMagicCapacity(itemStack);

                        for (DestabilizationEffect effect: DestabilizationEffect.getEffectsForImprovement(instability, module.getImprovements(itemStack))) {
                            int currentEffectLevel = module.getImprovementLevel(itemStack, effect.destabilizationKey);
                            int newLevel;

                            if (currentEffectLevel >= 0) {
                                newLevel = currentEffectLevel + 1;
                            } else if (effect.minLevel == effect.maxLevel) {
                                newLevel = effect.minLevel;
                            } else {
                                newLevel = effect.minLevel + world.rand.nextInt(effect.maxLevel - effect.minLevel);
                            }

                            if (module.acceptsImprovementLevel(effect.destabilizationKey, newLevel)
                                    && effect.probability * instability * probabilityMultiplier > world.rand.nextFloat()) {
                                module.addImprovement(itemStack, effect.destabilizationKey, newLevel);
                            }
                        }
                    });
        }
    }

    public void tweak(ItemStack itemStack, String slot, Map<String, Integer> tweaks) {
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
    public Multimap<Attribute, AttributeModifier> getEffectAttributes(ItemStack itemStack) {
        return AttributeHelper.emptyMap;
    }


    public Multimap<Attribute, AttributeModifier> getModuleAttributes(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getAttributeModifiers(itemStack))
                .filter(Objects::nonNull)
                .reduce(null, AttributeHelper::merge);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> attributes = AttributeHelper.merge(
                getModuleAttributes(itemStack),
                getEffectAttributes(itemStack));

        return Arrays.stream(getSynergyData(itemStack))
                .map(synergy -> synergy.attributes)
                .filter(Objects::nonNull)
                .reduce(attributes, AttributeHelper::merge);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiersCollapsed(ItemStack itemStack) {
        logger.debug("Gathering attribute modifiers for {} ({})", getDisplayName(itemStack).getString(), getDataCacheKey(itemStack));
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

    public Multimap<Attribute, AttributeModifier> getAttributeModifiersCached(ItemStack itemStack) {
        try {
            return attributeCache.get(getDataCacheKey(itemStack),
                    () -> Optional.ofNullable(getAttributeModifiersCollapsed(itemStack)).orElseGet(ImmutableMultimap::of));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return getAttributeModifiersCollapsed(itemStack);
        }
    }

    public double getAttributeValue(ItemStack itemStack, Attribute attribute) {
        if (isBroken(itemStack)) {
            return 0;
        }

        return AttributeHelper.getMergedAmount(getAttributeModifiersCached(itemStack).get(attribute));
    }

    public double getAttributeValue(ItemStack itemStack, Attribute attribute, double base) {
        if (isBroken(itemStack)) {
            return 0;
        }

        return AttributeHelper.getMergedAmount(getAttributeModifiersCached(itemStack).get(attribute), base);
    }

    public ToolData getToolData(ItemStack itemStack) {
        logger.debug("Gathering tool data for {} ({})", getDisplayName(itemStack).getString(), getDataCacheKey(itemStack));
        return Stream.concat(
                getAllModules(itemStack).stream()
                        .map(module -> module.getToolData(itemStack)),
                Arrays.stream(getSynergyData(itemStack))
                        .map(synergy -> synergy.tools))
                .filter(Objects::nonNull)
                .reduce(null, ToolData::merge);
    }

    public ToolData getToolDataCached(ItemStack itemStack) {
        try {
            return toolCache.get(getDataCacheKey(itemStack),
                    () -> Optional.ofNullable(getToolData(itemStack)).orElseGet(ToolData::new));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Optional.ofNullable(getToolData(itemStack)).orElseGet(ToolData::new);
        }
    }

    public EffectData getEffectData(ItemStack itemStack) {
        logger.debug("Gathering effect data for {} ({})", getDisplayName(itemStack).getString(), getDataCacheKey(itemStack));
        return Stream.concat(
                getAllModules(itemStack).stream()
                        .map(module -> module.getEffectData(itemStack)),
                Arrays.stream(getSynergyData(itemStack))
                        .map(synergy -> synergy.effects))
                .filter(Objects::nonNull)
                .reduce(null, EffectData::merge);
    }

    public EffectData getEffectDataCached(ItemStack itemStack) {
        try {
            return effectCache.get(getDataCacheKey(itemStack),
                    () -> Optional.ofNullable(getEffectData(itemStack)).orElseGet(EffectData::new));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Optional.ofNullable(getEffectData(itemStack)).orElseGet(EffectData::new);
        }
    }

    public ItemProperties getProperties(ItemStack itemStack) {
        logger.debug("Gathering properties for {} ({})", getDisplayName(itemStack).getString(), getDataCacheKey(itemStack));

        return Stream.concat(
                getAllModules(itemStack).stream().map(module -> module.getProperties(itemStack)),
                Arrays.stream(getSynergyData(itemStack)))
                .reduce(new ItemProperties(), ItemProperties::merge);
    }

    public ItemProperties getPropertiesCached(ItemStack itemStack) {
        try {
            return propertyCache.get(getDataCacheKey(itemStack), () -> getProperties(itemStack));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return getProperties(itemStack);
        }
    }

    @Override
    public int getToolLevel(ItemStack itemStack, ToolType tool) {
        if (isBroken(itemStack)) {
            return -1;
        }

        return getToolDataCached(itemStack).getLevel(tool);
    }

    @Override
    public float getToolEfficiency(ItemStack itemStack, ToolType tool) {
        if (isBroken(itemStack)) {
            return 0;
        }

        if (getToolLevel(itemStack, tool) <= 0) {
            return 0;
        }

        return getToolDataCached(itemStack).getEfficiency(tool);
    }

    @Override
    public Set<ToolType> getTools(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return Collections.emptySet();
        }

        return getToolDataCached(itemStack).getValues();
    }

    @Override
    public Map<ToolType, Integer> getToolLevels(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return Collections.emptyMap();
        }

        return getToolDataCached(itemStack).getLevelMap();
    }

    public int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        if (isBroken(itemStack)) {
            return -1;
        }

        return getEffectDataCached(itemStack).getLevel(effect);
    }

    public double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        if (isBroken(itemStack)) {
            return 0;
        }

        return getEffectDataCached(itemStack).getEfficiency(effect);
    }

    public Collection<ItemEffect> getEffects(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return Collections.emptyList();
        }

        return getEffectDataCached(itemStack).getValues();
    }

    /**
     * Vanilla method for determining if the item should display the enchantment glint
     * @param itemStack The itemstack for the item
     * @return true if should display glint
     */
    @Override
    public boolean hasEffect(@Nonnull ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .anyMatch(improvement -> improvement.enchantment);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public ImprovementData[] getImprovements(ItemStack itemStack) {
        return Arrays.stream(getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .flatMap(module -> Arrays.stream(module.getImprovements(itemStack)))
                .toArray(ImprovementData[]::new);
    }

    protected String getDisplayNamePrefixes(ItemStack itemStack) {
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

    @Override
    public ITextComponent getDisplayName(ItemStack itemStack) {
        // todo: since getItemStackDisplayName is called on the server we cannot use the I18n service
        if (Environment.get().getDist().isDedicatedServer()) {
            return new StringTextComponent("");
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
        return new StringTextComponent(WordUtils.capitalize(prefixes + name));
    }

    public SynergyData[] getAllSynergyData(ItemStack itemStack) {
        return synergies;
    }

    public SynergyData[] getSynergyData(ItemStack itemStack) {
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

    protected boolean hasImprovementSynergy(SynergyData synergy, String[] improvements) {
        int improvementMatches = 0;
        for (String improvement : improvements) {
            if (improvementMatches == synergy.moduleVariants.length) {
                break;
            }

            if (improvement.equals(synergy.moduleVariants[improvementMatches])) {
                improvementMatches++;
            }
        }

        return synergy.moduleVariants.length > 0 && improvementMatches == synergy.moduleVariants.length;
    }

    protected boolean hasVariantSynergy(SynergyData synergy, String[] variantKeys) {
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

    protected boolean hasModuleSynergy(ItemStack itemStack, SynergyData synergy, ItemModule[] modules) {
        int moduleMatches = 0;
        String variant = null;

        if (synergy.sameVariant) {
            for (ItemModule module : modules) {
                if (moduleMatches == synergy.modules.length) {
                    break;
                }

                if (module.getUnlocalizedName().equals(synergy.modules[moduleMatches])) {
                    if (synergy.sameVariant) {
                        if (variant == null) {
                            variant = module.getVariantData(itemStack).key;
                        }

                        if (variant.equals(module.getVariantData(itemStack).key)) {
                            moduleMatches++;
                        }
                    } else {
                        moduleMatches++;
                    }
                }
            }
        } else {
            for (ItemModule module : modules) {
                if (moduleMatches == synergy.modules.length) {
                    break;
                }

                if (module.getUnlocalizedName().equals(synergy.modules[moduleMatches])) {
                    if (synergy.sameVariant) {
                        if (variant == null) {
                            variant = module.getVariantData(itemStack).key;
                        }

                        if (variant.equals(module.getVariantData(itemStack).key)) {
                            moduleMatches++;
                        }
                    } else {
                        moduleMatches++;
                    }
                }
            }
        }

        return synergy.modules.length > 0 && moduleMatches == synergy.modules.length;
    }
}
