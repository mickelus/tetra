package se.mickelus.tetra.items.modular.impl.bow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import se.mickelus.tetra.compat.forge.registries.ObjectHolder;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.FocusEffect;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.event.ModularLooseProjectilesEvent;
import se.mickelus.tetra.event.ModularProjectileSpawnEvent;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.model.FilteredGridTextureModelData;
import se.mickelus.tetra.module.model.GridTextureModelData;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ModularBowItem extends ModularItem {
    public final static String staveKey = "bow/stave";
    public final static String stringKey = "bow/string";
    public final static String riserKey = "bow/riser";

    public static final String identifier = "modular_bow";
    public static final double velocityFactor = 1 / 8d;
    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(1, 21, -11, -3);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(-14, 23);
    public static final int maxUseDuration = 37000;
    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static ModularBowItem instance;
    protected GridTextureModelData arrowModel0 = new GridTextureModelData(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "item/module/bow/arrow_0"));
    protected GridTextureModelData arrowModel1 = new GridTextureModelData(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "item/module/bow/arrow_1"));
    protected GridTextureModelData arrowModel2 = new GridTextureModelData(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "item/module/bow/arrow_2"));
    protected ItemStack vanillaBow;

    public ModularBowItem() {
        super(new Properties().stacksTo(1).fireResistant());
        instance = this;

        majorModuleKeys = new String[] { stringKey, staveKey };
        minorModuleKeys = new String[] { riserKey };

        requiredModules = new String[] { stringKey, staveKey };

        vanillaBow = new ItemStack(Items.BOW);

        updateConfig(ConfigHandler.HONE_BOW_BASE_DEFAULT, ConfigHandler.HONE_BOW_INTEGRITY_MULTIPLIER_DEFAULT);

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, identifier));
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getArrowVelocity(int charge, double strength, float velocityBonus, boolean suspend) {
        float velocity = (float) charge / 20.0F;

        velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;


        if (velocity > 1.0F) {
            velocity = 1.0F;
        }
        // increase velocity for bows that have a higher draw strength than vanilla bows (6 strength)
        velocity = velocity * (float) Math.max(1, 1 + (strength - 6) * velocityFactor);

        if (suspend && charge >= 20) {
            velocity *= 2;
        } else {
            velocity += velocity * velocityBonus;
        }

        return velocity;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("bow/"));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void clientInit() {
        super.clientInit();
        NeoForge.EVENT_BUS.register(new RangedFOVTransformer());
    }

    private boolean isMainhandAllowedAttribute(Attribute attribute) {
        return !attribute.equals(TetraAttributes.drawStrength.get()) && !attribute.equals(TetraAttributes.drawSpeed.get());
    }

    private boolean isOffhandAllowedAttribute(Attribute attribute) {
        return !attribute.equals(TetraAttributes.drawStrength.get())
                && !attribute.equals(TetraAttributes.drawSpeed.get())
                && !attribute.equals(Attributes.ATTACK_DAMAGE)
                && !attribute.equals(Attributes.ATTACK_SPEED);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return ItemAttributeModifiers.EMPTY;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        getAttributeModifiersCached(itemStack).forEach((attribute, modifier) -> {
            if (isMainhandAllowedAttribute(attribute)) {
                EquipmentSlotGroup slotGroup = isOffhandAllowedAttribute(attribute) ? EquipmentSlotGroup.HAND : EquipmentSlotGroup.MAINHAND;
                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, slotGroup);
            }
        });
        return builder.build();
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void releaseUsing(ItemStack itemStack, Level world, LivingEntity entity, int timeLeft) {
        int usedTicks = getUseDuration(itemStack) - timeLeft;
        if (getEffectLevel(itemStack, ItemEffect.overbowed) > 0 && exceedsOverbowedLimit(entity, itemStack, usedTicks)) {
            entity.stopUsingItem();
            // trigger a small cooldown here to avoid the bow getting drawn again instantly
            CastOptional.cast(entity, Player.class).ifPresent(player -> player.getCooldowns().addCooldown(this, 10));
        } else {
            fireArrow(itemStack, world, entity, timeLeft);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int count) {
        if (getEffectLevel(itemStack, ItemEffect.releaseLatch) > 0 && getProgress(itemStack, entity) >= 1) {
            entity.releaseUsingItem();
        } else {
            int usedTicks = getUseDuration(itemStack) - count;
            if (getEffectLevel(itemStack, ItemEffect.overbowed) > 0 && exceedsOverbowedLimit(entity, itemStack, usedTicks)) {
                entity.stopUsingItem();
                // trigger a small cooldown here to avoid the bow getting drawn again instantly
                CastOptional.cast(entity, Player.class).ifPresent(player -> player.getCooldowns().addCooldown(this, 10));
            }
        }
    }

    protected void fireArrow(ItemStack itemStack, Level world, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ItemStack ammoStack = player.getProjectile(vanillaBow);

            boolean playerInfinite = isInfinite(player, itemStack, ammoStack);

            // multiply by 20 to align progress with vanilla bow (fully drawn at 1sec/20ticks)
            int drawProgress = Math.round(getProgress(itemStack, entity) * 20);
            drawProgress = EventHooks.onArrowLoose(itemStack, world, player, drawProgress,
                    !ammoStack.isEmpty() || playerInfinite);

            if (drawProgress < 0) {
                return;
            }

            if (!ammoStack.isEmpty() || playerInfinite) {
                if (ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }

                double strength = getDrawStrength(entity, itemStack);
                float velocityBonus = getEffectLevel(itemStack, ItemEffect.velocity) / 100f;
                int suspendLevel = getEffectLevel(itemStack, ItemEffect.suspend);
                ArrowItem ammoItem = CastOptional.cast(ammoStack.getItem(), ArrowItem.class)
                        .orElse((ArrowItem) Items.ARROW);
                boolean infiniteAmmo = player.getAbilities().instabuild || ammoItem.isInfinite(ammoStack, itemStack, player);

                ModularLooseProjectilesEvent looseProjectilesEvent = new ModularLooseProjectilesEvent(itemStack, ammoStack, player, world,
                        drawProgress, strength, suspendLevel > 0,
                        getArrowVelocity(drawProgress, strength, getEffectLevel(itemStack, ItemEffect.velocity) / 100f, suspendLevel > 0),
                        getEffectEfficiency(itemStack, ItemEffect.multishot),
                        Math.max(0, 100 - getEffectEfficiency(itemStack, ItemEffect.spread) - FocusEffect.getSpreadReduction(player, itemStack)),
                        player.getAbilities().instabuild || ammoItem.isInfinite(ammoStack, itemStack, player),
                        Mth.clamp(getEffectLevel(itemStack, ItemEffect.multishot), 1, infiniteAmmo ? 64 : ammoStack.getCount()),
                        player.getXRot(),
                        player.getYRot());
                NeoForge.EVENT_BUS.post(looseProjectilesEvent);

                ammoStack = looseProjectilesEvent.getAmmoStack();
                ImmutableList<Function<AbstractArrow, AbstractArrow>> projectileRemappers = looseProjectilesEvent.getProjectileRemappers();

                strength = looseProjectilesEvent.getStrength();
                boolean hasSuspend = looseProjectilesEvent.isHasSuspend();
                float projectileVelocity = looseProjectilesEvent.getProjectileVelocity();
                double multishotSpread = looseProjectilesEvent.getMultishotSpread();
                float accuracy = looseProjectilesEvent.getAccuracy();
                infiniteAmmo = looseProjectilesEvent.isInfiniteAmmo();
                int count = looseProjectilesEvent.getCount();
                double basePitch = looseProjectilesEvent.getBasePitch();
                double baseYaw = looseProjectilesEvent.getBaseYaw();

                if (projectileVelocity > 0.1f) {
                    if (!world.isClientSide) {
                        int piercingLevel = getEffectLevel(itemStack, ItemEffect.piercing)
                                + EffectHelper.getEnchantmentLevel(Enchantments.PIERCING, itemStack);

                        for (int i = 0; i < count; i++) {
                            double yaw = baseYaw - multishotSpread * (count - 1) / 2f + multishotSpread * i;
                            fireProjectile(itemStack, world, (ArrowItem) ammoStack.getItem(), ammoStack, projectileRemappers, player,
                                    (float) basePitch, (float) yaw, projectileVelocity, accuracy, drawProgress, strength, piercingLevel,
                                    hasSuspend, infiniteAmmo);
                        }


                        applyDamage(1, itemStack, player);
                        applyNegativeUsageEffects(entity, itemStack, 1);

                        // max draw at 20, has to be drawn at least 3/4th for positive effects
                        if (drawProgress > 15) {
                            applyPositiveUsageEffects(entity, itemStack, 1);
                        }
                    }

                    float pitchBase = projectileVelocity;
                    if (velocityBonus > 0) {
                        pitchBase -= pitchBase * velocityBonus;
                    } else if (hasSuspend) {
                        pitchBase = pitchBase / 2;
                    }
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                            0.8F + projectileVelocity * 0.2f,
                            1.9f + world.random.nextFloat() * 0.2F - pitchBase * 0.8F);

                    if (!infiniteAmmo && !player.getAbilities().instabuild) {
                        ammoStack.shrink(count);
                        if (ammoStack.isEmpty()) {
                            player.getInventory().removeItem(ammoStack);
                        }
                    }

                    FocusEffect.onFireArrow(player, itemStack);

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private double getDrawStrength(LivingEntity entity, ItemStack itemStack) {
        AttributeInstance instance = entity.getAttribute(AttributeHelper.getHolder(TetraAttributes.drawStrength.get()));
        if (instance != null) {
            return AttributeHelper.calculateValue(TetraAttributes.drawStrength.get(),
                    instance.getModifiers(),
                    getAttributeModifiersCached(itemStack).get(TetraAttributes.drawStrength.get()));
        }
        return getAttributeValue(itemStack, TetraAttributes.drawStrength.get());
    }

    private double getDrawSpeed(LivingEntity entity, ItemStack itemStack) {
        AttributeInstance instance = entity.getAttribute(AttributeHelper.getHolder(TetraAttributes.drawSpeed.get()));
        if (instance != null) {
            return AttributeHelper.calculateValue(TetraAttributes.drawSpeed.get(),
                    instance.getModifiers(),
                    getAttributeModifiersCached(itemStack).get(TetraAttributes.drawSpeed.get()));
        }
        return getAttributeValue(itemStack, TetraAttributes.drawSpeed.get());
    }

    public static void fireProjectile(ItemStack itemStack, Level world, ArrowItem ammoItem, ItemStack ammoStack,
            ImmutableList<Function<AbstractArrow, AbstractArrow>> projectileRemappers, Player player,
            float basePitch, float yaw, float projectileVelocity, float accuracy, int drawProgress, double strength, int piercingLevel,
            boolean hasSuspend, boolean infiniteAmmo) {
        AbstractArrow projectile = ammoItem.createArrow(world, ammoStack, player, itemStack);
        for (Function<AbstractArrow, AbstractArrow> remapper : projectileRemappers) {
            projectile = remapper.apply(projectile);
        }
        projectile.shootFromRotation(player, basePitch, yaw, 0.0F, projectileVelocity * 3.0F, accuracy);

        if (drawProgress >= 20) {
            projectile.setCritArrow(true);
        }

        // the damage modifier is based on fully drawn damage, vanilla bows deal 3 times base damage + 0-4 crit damage
        projectile.setBaseDamage(projectile.getBaseDamage() - 2 + strength / 3);

        // velocity multiplies arrow damage for vanilla projectiles, need to reduce damage if velocity > 1
        if (projectileVelocity > 1) {
            projectile.setBaseDamage(projectile.getBaseDamage() / projectileVelocity);
        }

        if (piercingLevel > 0) {
            projectile.setPierceLevel((byte) piercingLevel);
        }

        if (hasSuspend && drawProgress >= 20) {
            projectile.setNoGravity(true);
        }

        if (infiniteAmmo || player.getAbilities().instabuild
                && (ammoStack.getItem() == Items.SPECTRAL_ARROW || ammoStack.getItem() == Items.TIPPED_ARROW)) {
            projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }

        if (hasSuspend && drawProgress >= 20) {
            Vec3 projDir = projectile.getDeltaMovement().normalize();
            Vec3 projPos = projectile.position();
            for (int j = 0; j < 4; j++) {
                Vec3 pos = projPos.add(projDir.scale(2 + j * 2));
                ((ServerLevel) world).sendParticles(ParticleTypes.END_ROD,
                        pos.x(), pos.y(), pos.z(), 1,
                        0, 0, 0, 0.01);
            }
        }

        world.addFreshEntity(projectile);
        ModularProjectileSpawnEvent event = new ModularProjectileSpawnEvent(itemStack, ammoStack, player, projectile, world, drawProgress);
        NeoForge.EVENT_BUS.post(event);

        // vanilla velocity sync breaks when velocity is >3.9 on any axis
        if (projectileVelocity * 3 > 4) {
            TetraMod.packetHandler.sendToAllPlayersNear(new ProjectileMotionPacket(projectile), projectile.blockPosition(), 512, world.dimension());
        }
    }

    private boolean isInfinite(Player player, ItemStack bowStack, ItemStack ammoStack) {
        return player.getAbilities().instabuild
                || (ammoStack.isEmpty() && EffectHelper.getEnchantmentLevel(Enchantments.INFINITY, bowStack) > 0)
                || CastOptional.cast(ammoStack.getItem(), ArrowItem.class)
                .map(item -> item.isInfinite(ammoStack, bowStack, player))
                .orElse(false);
    }

    public int getDrawDuration(LivingEntity entity, ItemStack itemStack) {
        return Math.max((int) (20 * (getDrawSpeed(entity, itemStack)
                - EffectHelper.getEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack) * 0.2)), 1);
    }

    /**
     * Returns a value representing how far the bow has been drawn, 0 means the bow is not drawn while a value of 1 means that the bow is fully drawn.
     * Can exceed 1 when a draw is held longer than neccessary.
     *
     * @param itemStack
     * @param entity
     * @return
     */
    public float getProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Optional.ofNullable(entity)
                .filter(e -> e.getUseItemRemainingTicks() > 0)
                .filter(e -> itemStack.equals(e.getUseItem()))
                .map(e -> (getUseDuration(itemStack) - e.getUseItemRemainingTicks()) * 1f / getDrawDuration(e, itemStack))
                .orElse(0f);
    }

    public float getOverbowProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        int overbowedLevel = getEffectLevel(itemStack, ItemEffect.overbowed);
        if (overbowedLevel > 0 && entity != null && itemStack.equals(entity.getUseItem())) {
            int overbowedLimit = getOverbowedLimit(overbowedLevel);
            int drawDuration = getDrawDuration(entity, itemStack);
            int usedTicks = getUseDuration(itemStack) - entity.getUseItemRemainingTicks();
            return Mth.clamp(1f * (usedTicks - drawDuration) / overbowedLimit, 0, 1);
        }
        return 0;
    }

    /**
     * Assuming an overbowed bow, this represents the number of ticks the bow can be held after reaching full draw
     */
    public int getOverbowedLimit(int overbowedLevel) {
        return overbowedLevel * 2;
    }

    public int getOverbowedLimit(ItemStack itemStack) {
        return getOverbowedLimit(getEffectLevel(itemStack, ItemEffect.overbowed));
    }


    public boolean exceedsOverbowedLimit(LivingEntity entity, ItemStack itemStack, int usedTicks) {
        return usedTicks > getOverbowedLimit(itemStack) + getDrawDuration(entity, itemStack);
    }

    public int getUseDuration(ItemStack pStack) {
        return maxUseDuration;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return getUseDuration(stack);
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasAmmo = !player.getProjectile(vanillaBow).isEmpty();

        if (isBroken(bowStack)) {
            return InteractionResultHolder.pass(bowStack);
        }

        InteractionResultHolder<ItemStack> ret = EventHooks.onArrowNock(bowStack, world, player, hand, hasAmmo);
        if (ret != null) return ret;

        if (!hasAmmo && !player.getAbilities().instabuild && EffectHelper.getEnchantmentLevel(Enchantments.INFINITY, bowStack) <= 0) {
            return InteractionResultHolder.fail(bowStack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        }
    }

    private String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = getProgress(itemStack, entity);

        if (progress == 0) {
            return "undrawn";
        } else if (progress < 0.65) {
            return "draw_0";
        } else if (progress < 0.9) {
            return "draw_1";
        }
        return "draw_2";
    }

    private GridTextureModelData getArrowModel(String drawVariant) {
        switch (drawVariant) {
            case "draw_0":
                return arrowModel0;
            case "draw_1":
                return arrowModel1;
            case "draw_2":
                return arrowModel2;
            default:
                return arrowModel0;
        }
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return super.getModelCacheKey(itemStack, entity) + ":" + getDrawVariant(itemStack, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        String modelType = getDrawVariant(itemStack, entity);

        ImmutableList<IModuleModel> models = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .map(itemModule -> itemModule.getModels(itemStack))
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(IModuleModel::getRenderLayer))
                .filter(model -> filterModel(model, modelType))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        if (!modelType.equals("undrawn")) {
            return ImmutableList.<IModuleModel>builder()
                    .addAll(models)
                    .add(getArrowModel(modelType))
                    .build();
        }

        return models;
    }

    private static boolean filterModel(IModuleModel model, String filter) {
        return !(model instanceof FilteredGridTextureModelData filteredModel) || filteredModel.getFilter().equals(filter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }
}
