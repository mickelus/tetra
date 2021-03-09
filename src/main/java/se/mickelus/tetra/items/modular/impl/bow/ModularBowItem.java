package se.mickelus.tetra.items.modular.impl.bow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ModularBowItem extends ModularItem {
    public final static String staveKey = "bow/stave";
    public final static String stringKey = "bow/string";
    public final static String riserKey = "bow/riser";

    public static final String unlocalizedName = "modular_bow";

    protected ModuleModel arrowModel0 = new ModuleModel("draw_0", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_0"));
    protected ModuleModel arrowModel1 = new ModuleModel("draw_1", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_1"));
    protected ModuleModel arrowModel2 = new ModuleModel("draw_2", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_2"));

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(1, 21, -11, -3);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(-14, 23);

    public static final double velocityFactor = 1 / 8d;

    protected ItemStack vanillaBow;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularBowItem instance;

    public ModularBowItem() {
        super(new Properties().maxStackSize(1).isImmuneToFire());
        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { stringKey, staveKey };
        minorModuleKeys = new String[] { riserKey };

        requiredModules = new String[] { stringKey, staveKey };

        vanillaBow = new ItemStack(Items.BOW);

        updateConfig(ConfigHandler.honeBowBase.get(), ConfigHandler.honeBowIntegrityMultiplier.get());

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this));
        RemoveSchematic.registerRemoveSchematics(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void clientInit() {
        super.clientInit();
        MinecraftForge.EVENT_BUS.register(new RangedProgressOverlay(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new RangedFOVTransformer());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return AttributeHelper.emptyMap;
        }

        if (slot == EquipmentSlotType.MAINHAND) {
            return getAttributeModifiersCached(itemStack);
        }

        if (slot == EquipmentSlotType.OFFHAND) {
            return getAttributeModifiersCached(itemStack).entries().stream()
                    .filter(entry -> !(entry.getKey().equals(Attributes.ATTACK_DAMAGE) || entry.getKey().equals(Attributes.ATTACK_DAMAGE)))
                    .collect(Multimaps.toMultimap(Map.Entry::getKey, Map.Entry::getValue, ArrayListMultimap::create));
        }

        return AttributeHelper.emptyMap;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        if (getEffectLevel(itemStack, ItemEffect.overbowed) > 0 && timeLeft <= 0) {
            entity.resetActiveHand();
            // trigger a small cooldown here to avoid the bow getting drawn again instantly
            CastOptional.cast(entity, PlayerEntity.class).ifPresent(player -> player.getCooldownTracker().setCooldown(this, 10));
        } else {
            fireArrow(itemStack, world, entity, timeLeft);
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack itemStack, World world, LivingEntity entity) {
        if (getEffectLevel(itemStack, ItemEffect.overbowed) > 0) {
            entity.resetActiveHand();
            CastOptional.cast(entity, PlayerEntity.class).ifPresent(player -> player.getCooldownTracker().setCooldown(this, 10));
        }

        return super.onItemUseFinish(itemStack, world, entity);
    }

    @Override
    public void onUsingTick(ItemStack itemStack, LivingEntity player, int count) {
        if (getEffectLevel(itemStack, ItemEffect.releaseLatch) > 0 && getProgress(itemStack, player) >= 1) {
            player.stopActiveHand();
        }
    }

    protected void fireArrow(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            boolean playerInfinite = player.abilities.isCreativeMode
                    || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, itemStack) > 0;
            ItemStack ammoStack = player.findAmmo(vanillaBow);

            // multiply by 20 to align progress with vanilla bow (fully drawn at 1sec/20ticks)
            int drawProgress = Math.round(getProgress(itemStack, entity) * 20);
            drawProgress = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(itemStack, world, player, drawProgress,
                    !ammoStack.isEmpty() || playerInfinite);

            if (drawProgress < 0) {
                return;
            }

            if (!ammoStack.isEmpty() || playerInfinite) {
                if (ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }

                double strength = getAttributeValue(itemStack, TetraAttributes.drawStrength.get());
                float velocityBonus = getEffectLevel(itemStack, ItemEffect.velocity) / 100f;
                int suspendLevel = getEffectLevel(itemStack, ItemEffect.suspend);
                float projectileVelocity = getArrowVelocity(drawProgress, strength, velocityBonus, suspendLevel > 0);

                if (projectileVelocity > 0.1f) {
                    ArrowItem ammoItem = CastOptional.cast(ammoStack.getItem(), ArrowItem.class)
                            .orElse((ArrowItem) Items.ARROW);

                    boolean infiniteAmmo = player.abilities.isCreativeMode || ammoItem.isInfinite(ammoStack, itemStack, player);
                    int count = MathHelper.clamp(getEffectLevel(itemStack, ItemEffect.multishot), 1, infiniteAmmo ? 64 : ammoStack.getCount());

                    if (!world.isRemote) {
                        double spread = getEffectEfficiency(itemStack, ItemEffect.multishot);

                        int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, itemStack);
                        int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, itemStack);
                        int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, itemStack);
                        int piercingLevel = getEffectLevel(itemStack, ItemEffect.piercing);

                        for (int i = 0; i < count; i++) {
                            double yaw = player.rotationYaw - spread * (count - 1) / 2f + spread * i;
                            AbstractArrowEntity projectile = ammoItem.createArrow(world, ammoStack, player);
                            projectile.func_234612_a_(player, player.rotationPitch, (float) yaw, 0.0F, projectileVelocity * 3.0F, 1.0F);

                            if (drawProgress >= 20) {
                                projectile.setIsCritical(true);
                            }

                            // the damage modifier is based on fully drawn damage, vanilla bows deal 3 times base damage + 0-4 crit damage
                            projectile.setDamage(projectile.getDamage() -2 + strength / 3);

                            if (powerLevel > 0) {
                                projectile.setDamage(projectile.getDamage() + powerLevel * 0.5D + 0.5D);
                            }

                            // velocity multiplies arrow damage for vanilla projectiles, need to reduce damage if velocity > 1
                            if (projectileVelocity > 1) {
                                projectile.setDamage(projectile.getDamage() / projectileVelocity);
                            }

                            if (punchLevel > 0) {
                                projectile.setKnockbackStrength(punchLevel);
                            }

                            if (flameLevel > 0) {
                                projectile.setFire(100);
                            }

                            if (piercingLevel > 0) {
                                projectile.setPierceLevel((byte) piercingLevel);
                            }

                            if (suspendLevel > 0 && drawProgress >= 20) {
                                projectile.setNoGravity(true);
                            }

                            if (infiniteAmmo || player.abilities.isCreativeMode
                                    && (ammoStack.getItem() == Items.SPECTRAL_ARROW || ammoStack.getItem() == Items.TIPPED_ARROW)) {
                                projectile.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                            }

                            if (suspendLevel > 0 && drawProgress >= 20) {
                                Vector3d projDir = projectile.getMotion().normalize();
                                Vector3d projPos = projectile.getPositionVec();
                                for (int j = 0; j < 4; j++) {
                                    Vector3d pos = projPos.add(projDir.scale(2 + j * 2));
                                    ((ServerWorld)entity.world).spawnParticle(ParticleTypes.END_ROD,
                                            pos.getX(), pos.getY(), pos.getZ(), 1,
                                            0, 0, 0, 0.01);
                                }
                            }

                            world.addEntity(projectile);
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
                    } else if (suspendLevel > 0) {
                        pitchBase = pitchBase / 2;
                    }
                    world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(),
                            SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                            0.8F + projectileVelocity * 0.2f,
                            1.9f + random.nextFloat() * 0.2F - pitchBase * 0.8F);

                    if (!infiniteAmmo && !player.abilities.isCreativeMode) {
                        ammoStack.shrink(count);
                        if (ammoStack.isEmpty()) {
                            player.inventory.deleteStack(ammoStack);
                        }
                    }

                    player.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getArrowVelocity(int charge, double strength, float velocityBonus, boolean suspend) {
        float velocity = (float)charge / 20.0F;

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

    public int getDrawDuration(ItemStack itemStack) {
        return Math.max((int) (20 * getAttributeValue(itemStack, TetraAttributes.drawSpeed.get())), 1);
    }

    /**
     * Returns a value between 0 - 1 representing how far the bow has been drawn, a value of 1 means that the bow is fully drawn
     * @param itemStack
     * @param entity
     * @return
     */
    public float getProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Optional.ofNullable(entity)
                .filter(e -> e.getItemInUseCount() > 0)
                .filter(e -> itemStack.equals(e.getActiveItemStack()))
                .map( e -> (getUseDuration(itemStack) - e.getItemInUseCount()) * 1f / getDrawDuration(itemStack))
                .orElse(0f);
    }

    public float getOverbowProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        int overbowedLevel = getEffectLevel(itemStack, ItemEffect.overbowed);
        if (overbowedLevel > 0) {
            return Optional.ofNullable(entity)
                    .filter(e -> itemStack.equals(e.getActiveItemStack()))
                    .map(LivingEntity::getItemInUseCount)
                    .map(useCount -> 1 - useCount / (overbowedLevel * 2f))
                    .map(progress -> MathHelper.clamp(progress, 0, 1))
                    .orElse(0f);
        }

        return 0;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getUseDuration(ItemStack itemStack) {
        int overbowedLevel = getEffectLevel(itemStack, ItemEffect.overbowed);
        if (overbowedLevel > 0) {
            // each level equals a 0.1 seconds, times 20 ticks per second = 2
            return overbowedLevel * 2 + getDrawDuration(itemStack);
        }

        return 37000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack bowStack = player.getHeldItem(hand);
        boolean hasAmmo = !player.findAmmo(vanillaBow).isEmpty();

        if (isBroken(bowStack)) {
            return ActionResult.resultPass(bowStack);
        }

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(bowStack, world, player, hand, hasAmmo);
        if (ret != null) return ret;

        if (!player.abilities.isCreativeMode && !hasAmmo) {
            return ActionResult.resultFail(bowStack);
        } else {
            player.setActiveHand(hand);
            return ActionResult.resultConsume(bowStack);
        }
    }

    private String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = getProgress(itemStack, entity);

        if (progress == 0) {
            return "item";
        } else if (progress < 0.65) {
            return "draw_0";
        } else if (progress < 0.9) {
            return "draw_1";
        }
        return "draw_2";
    }

    private ModuleModel getArrowModel(String drawVariant) {
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
        return super.getModelCacheKey(itemStack, entity) + ":" +  getDrawVariant(itemStack, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        String modelType = getDrawVariant(itemStack, entity);

        ImmutableList<ModuleModel> models = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                .filter(Objects::nonNull)
                .filter(model -> model.type.equals(modelType) || model.type.equals("static"))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        if (!modelType.equals("item")) {
            return ImmutableList.<ModuleModel>builder()
                    .addAll(models)
                    .add(getArrowModel(modelType))
                    .build();
        }

        return models;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets() {
        return minorOffsets;
    }
}
