package se.mickelus.tetra.items.modular;

import com.google.common.collect.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.SweepingEffect;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.NBTHelper;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.effect.potion.EarthboundPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.*;

public class ItemModularHandheld extends ModularItem {

    /**
     * Below are lists of blocks, materials and tags that describe what different tools can harvest and efficiently destroy. Note that these
     * are copies of what the vanilla tool counterparts explicitly state that they can destroy and harvest, some blocks (and required tiers)
     * are not listed here as that's part of that block's implementation.
     */

    private static final Set<Material> hoeBonusMaterials = Sets.newHashSet(Material.PLANTS, Material.TALL_PLANTS, Material.CORAL);

    private static final Set<Material> axeMaterials = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANTS, Material.TALL_PLANTS, Material.BAMBOO, Material.GOURD);
    private static final Set<Material> pickaxeMaterials = Sets.newHashSet(Material.IRON, Material.ANVIL, Material.ROCK);

    // copy of hardcoded values in SwordItem, materials & tag that it explicitly state it can efficiently DESTROY
    private static final Set<Material> cuttingDestroyMaterials = Sets.newHashSet(Material.PLANTS, Material.TALL_PLANTS, Material.CORAL, Material.GOURD, Material.WEB, Material.BAMBOO);
    private static final Set<ITag.INamedTag<Block>> cuttingDestroyTags = Sets.newHashSet(BlockTags.LEAVES);

    // copy of hardcoded values in SwordItem, blocks that the sword explicitly state it can efficiently HARVEST
    private static final Set<Block> cuttingHarvestBlocks = Sets.newHashSet(Blocks.COBWEB);

    public static final ResourceLocation nailedTag = new ResourceLocation("tetra:nailed");

    // the base amount of damage the item should take after destroying a block
    protected int blockDestroyDamage = 1;

    // the base amount of damage the item should take after hitting an entity
    protected int entityHitDamage = 1;

    // if the blocking level exceeds this value the item has an infinite blocking duration
    public static final int blockingDurationLimit = 16;

    public ItemModularHandheld(Properties properties) {
        super(properties);
    }

    public int getBlockDestroyDamage() {
        return blockDestroyDamage;
    }

    public int getEntityHitDamage() {
        return entityHitDamage;
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getBlockHardness(world, pos) > 0) {
            applyDamage(blockDestroyDamage, itemStack, entity);

            if (!isBroken(itemStack)) {
                applyUsageEffects(entity, itemStack, 1);
            }
        }

        applyBreakEffects(itemStack, world, state, pos, entity);

        return true;
    }

    public void applyBreakEffects(ItemStack itemStack, World world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!world.isRemote) {
            int intuitLevel = getEffectLevel(itemStack, ItemEffect.intuit);
            if (intuitLevel > 0) {
                int xp = state.getExpDrop(world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemStack),
                        EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack));
                if (xp > 0) {
                    tickHoningProgression(entity, itemStack, xp);
                }
            }
        }
    }

    @Override
    public boolean hitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        applyDamage(entityHitDamage, itemStack, attacker);

        if (!isBroken(itemStack)) {
            float attackStrength = CastOptional.cast(attacker, PlayerEntity.class)
                    .map(EffectHelper::getCooledAttackStrength)
                    .orElse(1f);

            if (attackStrength > 0.9) {
                int sweepingLevel = getEffectLevel(itemStack, ItemEffect.sweeping);
                if (sweepingLevel > 0) {
                    SweepingEffect.sweepAttack(itemStack, target, attacker, sweepingLevel);
                }
            }

            ItemEffectHandler.applyHitEffects(itemStack, target, attacker);
            applyUsageEffects(attacker, itemStack, 1);
        }

        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        ItemStack itemStack = player.getHeldItem(hand);

        BlockState blockState = world.getBlockState(pos);

        if (isBroken(itemStack)) {
            return ActionResultType.PASS;
        }

        boolean canChannel = getUseDuration(itemStack) > 0;
        if (!canChannel || player.isCrouching()) {
            ToolData toolData = getToolDataCached(itemStack);
            for (ToolType tool: toolData.getValues()) {
                if (toolData.getLevel(tool) > 0) {
                    BlockState block = blockState.getToolModifiedState(world, pos, context.getPlayer(), context.getItem(), tool);
                    if (block != null) {
                        SoundEvent sound = Optional.ofNullable(getUseSound(tool))
                                .orElseGet(() -> blockState.getSoundType(world, pos, player).getHitSound());

                        world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        if (!world.isRemote) {
                            world.setBlockState(pos, block, 11);
                            applyDamage(blockDestroyDamage, context.getItem(), player);
                            applyUsageEffects(player, itemStack, 2);
                        }

                        return ActionResultType.func_233537_a_(world.isRemote);
                    }
                }
            }

            int denailingLevel = getEffectLevel(itemStack, ItemEffect.denailing);
            if (denailingLevel > 0 && player.getCooledAttackStrength(0) > 0.9) {
                if (denailBlock(player, world, pos, hand, facing)) {
                    applyDamage(blockDestroyDamage, itemStack, player);
                    applyUsageEffects(player, itemStack, 2);
                    player.resetCooldown();
                    return ActionResultType.func_233537_a_(world.isRemote);
                }
            }
        }

        return super.onItemUse(context);
    }

    private SoundEvent getUseSound(ToolType tool) {
        if      (tool == ToolType.AXE)    return SoundEvents.ITEM_AXE_STRIP;
        else if (tool == ToolType.HOE)    return SoundEvents.ITEM_HOE_TILL;
        else if (tool == ToolType.SHOVEL) return SoundEvents.ITEM_SHOVEL_FLATTEN;
        return null;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);

        // pass success for channeled abilities
        if (getUseDuration(itemStack) > 0) {
            player.setActiveHand(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, itemStack);
        }

        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack itemStack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (!player.getCooldownTracker().hasCooldown(this) && !isBroken(itemStack)) {
            if (getUseDuration(itemStack) == 0 || player.isCrouching()) {
                int bashingLevel = getEffectLevel(itemStack, ItemEffect.bashing);
                if (bashingLevel > 0) {
                    bashEntity(itemStack, bashingLevel, player, target);

                    tickProgression(player, itemStack, 2);
                    applyDamage(2, itemStack, player);
                    return ActionResultType.func_233537_a_(player.world.isRemote);
                }

                if (Hand.OFF_HAND.equals(hand)) {
                    int jabLevel = getEffectLevel(itemStack, ItemEffect.jab);
                    if (jabLevel > 0) {
                        jabEntity(itemStack, jabLevel, player, target);

                        tickProgression(player, itemStack, 2);
                        applyDamage(2, itemStack, player);
                        return ActionResultType.func_233537_a_(player.world.isRemote);
                    }
                }
            }
        }

        return ActionResultType.PASS;
    }

    public boolean itemInteractionForEntitySecondary(ItemStack itemStack, PlayerEntity player, LivingEntity target, Hand hand) {
        int bashingLevel = getEffectLevel(itemStack, ItemEffect.bashing);
        if (bashingLevel > 0) {
            bashEntity(itemStack, bashingLevel, player, target);

            tickProgression(player, itemStack, 2);
            applyDamage(2, itemStack, player);
            return true;
        }

        return false;
    }

    public void jabEntity(ItemStack itemStack, int jabLevel, PlayerEntity player, LivingEntity target) {
        // apply damage
        float targetModifier = EnchantmentHelper.getModifierForCreature(itemStack, target.getCreatureAttribute());
        float critMultiplier = Optional.ofNullable(ForgeHooks.getCriticalHit(player, target, false, 1.5f))
                .map(CriticalHitEvent::getDamageModifier)
                .orElse(1f);

        double damage = (1 + getAbilityBaseDamage(itemStack) + targetModifier) * critMultiplier * jabLevel / 100f;
        target.attackEntityFrom(DamageSource.causePlayerDamage(player), (float) damage);

        // applies enchantment effects on both parties
        EnchantmentHelper.applyThornEnchantments(target, player);
        EffectHelper.applyEnchantmentHitEffects(itemStack, target, player);

        // tetra item effects
        ItemEffectHandler.applyHitEffects(itemStack, target, player);

        // knocks back the target based on effect level + knockback enchantment level
        float knockbackFactor = 0.5f + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack);
        target.applyKnockback(knockbackFactor * 0.2f,
                player.getPosX() - target.getPosX(), player.getPosZ() - target.getPosZ());

        if (targetModifier > 1) {
            player.onEnchantmentCritical(target);
        }

        if (critMultiplier > 1) {
            player.getEntityWorld().playSound(player, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1, 1.3f);
            player.onCriticalHit(target);
        } else {
            player.getEntityWorld().playSound(player, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 1.3f);
        }

        player.getCooldownTracker().setCooldown(this, (int) Math.round(getCooldownBase(itemStack) * 20));
    }

    public void bashEntity(ItemStack itemStack, int bashingLevel, PlayerEntity player, LivingEntity target) {
        // apply damage
        float targetModifier = EnchantmentHelper.getModifierForCreature(itemStack, target.getCreatureAttribute());
        float critMultiplier = Optional.ofNullable(ForgeHooks.getCriticalHit(player, target, false, 1.5f))
                .map(CriticalHitEvent::getDamageModifier)
                .orElse(1f);

        double damage = (getAbilityBaseDamage(itemStack) + targetModifier) * critMultiplier;
        target.attackEntityFrom(DamageSource.causePlayerDamage(player), (float) damage);

        // applies enchantment effects on both parties
        EnchantmentHelper.applyThornEnchantments(target, player);
        EffectHelper.applyEnchantmentHitEffects(itemStack, target, player);

        // tetra item effects
        ItemEffectHandler.applyHitEffects(itemStack, target, player);

        // knocks back the target based on effect level + knockback enchantment level
        int knockbackFactor = bashingLevel
                + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack)
                + (player.isSprinting() ? 1 : 0);
        target.applyKnockback(knockbackFactor * 0.5f,
                player.getPosX() - target.getPosX(), player.getPosZ() - target.getPosZ());

        // stuns the target if bash efficiency is > 0
        double stunDuration = getEffectEfficiency(itemStack, ItemEffect.bashing);
        if (stunDuration > 0) {
            target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, (int) Math.round(stunDuration * 20), 0, false, false));
        }

        if (targetModifier > 1) {
            player.onEnchantmentCritical(target);
        }

        if (critMultiplier > 1) {
            player.onCriticalHit(target);
        }

        player.getEntityWorld().playSound(player, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1, 0.7f);

        player.getCooldownTracker().setCooldown(this, (int) Math.round(getCooldownBase(itemStack) * 20));
    }

    public void throwItem(PlayerEntity player, ItemStack stack, int riptideLevel, float cooldownBase) {
        World world = player.world;
        if (!world.isRemote) {
            applyDamage(1, stack, player);
            applyUsageEffects(player, stack, 1);

            ThrownModularItemEntity projectileEntity = new ThrownModularItemEntity(world, player, stack);

            if (player.abilities.isCreativeMode) {
                projectileEntity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
            } else {
                player.inventory.deleteStack(stack);
            }

            projectileEntity.func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F + (float)riptideLevel * 0.5F, 1.0F);
            world.addEntity(projectileEntity);

            if (this instanceof ModularSingleHeadedItem) {
                world.playMovingSound(null, projectileEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else if (this instanceof ModularShieldItem) {
                world.playMovingSound(null, projectileEntity, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.PLAYERS, 1.0F, 2F);
            } else {
                world.playMovingSound(null, projectileEntity, SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.PLAYERS, 1.0F, 0.7F);
            }
        }

        player.getCooldownTracker().setCooldown(this, Math.round(cooldownBase * 20));
    }

    public void causeRiptideEffect(PlayerEntity player, int riptideLevel) {
        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;
        float x = -MathHelper.sin(yaw * ((float)Math.PI / 180F)) * MathHelper.cos(pitch * ((float)Math.PI / 180F));
        float y = -MathHelper.sin(pitch * ((float)Math.PI / 180F));
        float z = MathHelper.cos(yaw * ((float)Math.PI / 180F)) * MathHelper.cos(pitch * ((float)Math.PI / 180F));

        float velocityMultiplier = 3.0F * ((1.0F + riptideLevel) / 4.0F);

        // vanilla divides this by the length of the directional vector, but that should always be 1
        x = x * velocityMultiplier;
        y = y * velocityMultiplier;
        z = z * velocityMultiplier;
        player.addVelocity(x, y, z);
        player.startSpinAttack(20);
        if (player.isOnGround()) {
            player.move(MoverType.SELF, new Vector3d(0, 1.1999999, 0));
        }

        SoundEvent soundEvent;
        if (riptideLevel >= 3) {
            soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_3;
        } else if (riptideLevel == 2) {
            soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_2;
        } else {
            soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_1;
        }
        player.world.playMovingSound(null, player, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);

        player.addStat(Stats.ITEM_USED.get(this));

    }

    /**
     * Instantly break plank based blocks.
     * @param player the responsible player entity
     * @param world the world in which the action takes place
     * @param pos the position in the world
     * @param hand the hand holding the tool
     * @param facing the clicked face
     * @return true if the block can be (and was) denailed, otherwise false
     */
    public boolean denailBlock(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return false;
        }

        BlockState blockState = world.getBlockState(pos);
        if (canDenail(blockState)) {
            boolean success = EffectHelper.breakBlock(world, player, player.getHeldItem(hand), pos, blockState, true);
            if (success) {
                player.resetCooldown();
                return true;
            }
        }

        return false;
    }

    public static boolean canDenail(BlockState blockState) {
        return blockState.getBlock().getTags().contains(nailedTag);
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (getEffectLevel(stack, ItemEffect.blocking) > 0) {
            return UseAction.BLOCK;
        }

        if (getEffectLevel(stack, ItemEffect.throwable) > 0
                || EnchantmentHelper.getRiptideModifier(stack) > 0) {
            return UseAction.SPEAR;
        }

        return super.getUseAction(stack);
    }

    /**
     * Returns a value between 0 - 1 representing how long a blocking item has been raised
     * @param itemStack
     * @param entity
     * @return
     */
    public float getBlockProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        int blockingLevel = getEffectLevel(itemStack, ItemEffect.blocking);
        if (blockingLevel > 0 && blockingLevel < blockingDurationLimit) {
            return Optional.ofNullable(entity)
                    .filter(e -> e.getItemInUseCount() > 0)
                    .filter(e -> itemStack.equals(e.getActiveItemStack()))
                    .map( e -> (e.getItemInUseCount()) * 1f / getUseDuration(itemStack))
                    .orElse(0f);
        }

        return 0;
    }

    public boolean isThrowing(ItemStack itemStack, @Nullable LivingEntity entity) {
        return UseAction.SPEAR.equals(getUseAction(itemStack)) && Optional.ofNullable(entity)
                .map(e -> e.getItemInUseCount() > 0)
                .orElse(false);
    }

    @Override
    public boolean isShield(ItemStack itemStack, @Nullable LivingEntity entity) {
        return getEffectLevel(itemStack, ItemEffect.blocking) > 0;
    }

    /**
     * How long it takes to use this item (or how long it can be held at max)
     */
    @Override
    public int getUseDuration(ItemStack itemStack) {
        int blockingLevel = getEffectLevel(itemStack, ItemEffect.blocking);
        if (blockingLevel > 0) {
            int duration = blockingLevel * 20;
            return blockingLevel < blockingDurationLimit ? duration : 72000;
        }

        if (getEffectLevel(itemStack, ItemEffect.throwable) > 0
                || EnchantmentHelper.getRiptideModifier(itemStack) > 0) {
            return 72000;
        }

        return 0;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack itemStack, World world, LivingEntity entity) {
        CastOptional.cast(entity, PlayerEntity.class)
                .ifPresent(player -> {
                    double blockingLevel = getEffectLevel(itemStack, ItemEffect.blocking);
                    if (blockingLevel > 0) {
                        if (blockingLevel < blockingDurationLimit) {
                            player.getCooldownTracker().setCooldown(this, (int) Math.round(getCooldownBase(itemStack) * 20));
                        }

                        if (player.isCrouching() && world.isRemote) {
                            onPlayerStoppedUsingSecondary(itemStack, world, entity, 0);
                        }
                    }
                });

        return super.onItemUseFinish(itemStack, world, entity);
    }

    /**
     * Called when the player stops using/channeling an item, e.g. when throwing a trident.
     * @param stack
     * @param world
     * @param entityLiving
     * @param timeLeft
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            int ticksUsed = this.getUseDuration(stack) - timeLeft;

            double cooldownBase = getCooldownBase(stack);
            int blockingLevel = getEffectLevel(stack, ItemEffect.blocking);
            int throwingLevel = getEffectLevel(stack, ItemEffect.throwable);
            int riptideLevel = EnchantmentHelper.getRiptideModifier(stack);

            if (blockingLevel > 0) {
                if (blockingLevel < blockingDurationLimit) {
                    player.getCooldownTracker().setCooldown(this, (int) Math.round(cooldownBase * 20));
                }

                if (player.isCrouching()) {
                    if (ticksUsed >= 10 && riptideLevel > 0 && player.isWet()) {
                        causeRiptideEffect(player, riptideLevel);
                    } else if (ticksUsed >= 10 && throwingLevel > 0) {
                        throwItem(player, stack, riptideLevel, (float) cooldownBase);
                    } else if (world.isRemote) {
                        onPlayerStoppedUsingSecondary(stack, world, entityLiving, timeLeft);
                    }
                }
            } else if (ticksUsed >= 10) {
                if (riptideLevel > 0 && player.isWet()) {
                    causeRiptideEffect(player, riptideLevel);
                } else if (getEffectLevel(stack, ItemEffect.throwable) > 0) {
                    throwItem(player, stack, riptideLevel, (float) cooldownBase);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onPlayerStoppedUsingSecondary(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            LivingEntity target = Optional.ofNullable(Minecraft.getInstance().objectMouseOver)
                    .filter(rayTraceResult -> rayTraceResult.getType() == RayTraceResult.Type.ENTITY)
                    .map(rayTraceResult -> ((EntityRayTraceResult)rayTraceResult).getEntity())
                    .flatMap(hitEntity -> CastOptional.cast(hitEntity, LivingEntity.class))
                    .orElse(null);

            Hand activeHand = entity.getActiveHand();

            PacketHandler.sendToServer(new SecondaryAbilityPacket(target, activeHand));

            handleSecondaryAbility((PlayerEntity) entity, activeHand, target);
        }
    }

    public static void handleSecondaryAbility(PlayerEntity player, Hand hand, LivingEntity target) {
        ItemStack activeStack = player.getHeldItem(hand);

        if (!activeStack.isEmpty() && activeStack.getItem() instanceof ItemModularHandheld) {
            ItemModularHandheld item = (ItemModularHandheld) activeStack.getItem();

            if (target != null) {
                item.itemInteractionForEntitySecondary(activeStack, player, target, hand);

                player.resetActiveHand();
                player.getCooldownTracker().setCooldown(item, (int) Math.round(item.getCooldownBase(activeStack) * 20 * 1.5));
                player.swingArm(hand);
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        EffectHelper.setCooledAttackStrength(player, player.getCooledAttackStrength(0.5f));
        return false;
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
                    .filter(entry -> entry.getKey().equals(Attributes.ARMOR) || entry.getKey().equals(Attributes.ARMOR_TOUGHNESS))
                    .collect(Multimaps.toMultimap(Map.Entry::getKey, Map.Entry::getValue, ArrayListMultimap::create));
        }

        return AttributeHelper.emptyMap;
    }

    public double getAbilityBaseDamage(ItemStack itemStack) {
        return getAttributeValue(itemStack, Attributes.ATTACK_DAMAGE);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getEffectAttributes(ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        Optional.of(getCounterWeightBonus(itemStack))
                .filter(bonus -> bonus > 0)
                .map(bonus -> new AttributeModifier("counterweight", bonus, AttributeModifier.Operation.ADDITION))
                .ifPresent(modifier -> result.put(Attributes.ATTACK_SPEED, modifier));

        return result;
    }

    public double getCounterWeightBonus(ItemStack itemStack) {
        int counterWeightLevel = getEffectLevel(itemStack, ItemEffect.counterweight);
        if (counterWeightLevel > 0) {
            int integrityCost = getIntegrityCost(itemStack);

            return Math.max(0, 0.15 - Math.abs(counterWeightLevel - integrityCost) * 0.05);
        }
        return 0;
    }

    /**
     * Base cooldown value for abilities/usages related to this item, in ticks
     * @param itemStack
     * @return
     */
    public double getCooldownBase(ItemStack itemStack) {
        // base value for player attack speed is 4
        return 1 / Math.max(0.1, getAttributeValue(itemStack, Attributes.ATTACK_SPEED, 4) + getCounterWeightBonus(itemStack));
    }

    @Override
    public Set<ToolType> getToolTypes(ItemStack stack) {
        if (!isBroken(stack)) {
            return getTools(stack);
        }
        return Collections.emptySet();
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        if (!isBroken(stack)) {
            int toolTier = getToolLevel(stack, tool);
            if (toolTier > 0) {
                return toolTier - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        if (!state.getRequiresTool()) {
            return true;
        }

        ToolType requiredTool = state.getHarvestTool();
        if (requiredTool == null) {
            requiredTool = getEffectiveTool(state);
        }

        return requiredTool != null && getHarvestLevel(stack, requiredTool, null, state) >= Math.max(state.getHarvestLevel(), 0);
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if (!isBroken(itemStack)) {
            ToolType tool = getEffectiveTool(blockState);
            float speed = (float) (getAttributeValue(itemStack, Attributes.ATTACK_SPEED, 4) * 0.5 + 0.5);

            if (tool != null) {
                speed *= getToolEfficiency(itemStack, tool);
            } else {
                speed *= getToolTypes(itemStack).stream()
                        .filter(blockState::isToolEffective)
                        .map(toolType -> getToolEfficiency(itemStack, toolType))
                        .max(Comparator.naturalOrder())
                        .orElse(0f);
            }

            // todo: need a better way to handle how swords break stuff faster
            if (getToolLevel(itemStack, ToolTypes.cut) > 0) {
                if (blockState.getBlock().equals(Blocks.COBWEB)) {
                    speed *= 10;
                }

                if (blockState.getBlock().equals(Blocks.BAMBOO)) {
                    speed = 30; // makes swords instamine bamboo
                }
            }

            if (speed < 1) {
                return 1;
            }
            return speed;
        }
        return 1;
    }

    public static boolean isToolEffective(ToolType toolType, BlockState blockState) {
        if (ToolTypes.cut.equals(toolType)
                && (cuttingHarvestBlocks.contains(blockState.getBlock())
                    || cuttingDestroyMaterials.contains(blockState.getMaterial())
                    || cuttingDestroyTags.stream().anyMatch(tag -> blockState.getBlock().isIn(tag)))) {
            return true;
        }

        if (ToolType.HOE.equals(toolType) && hoeBonusMaterials.contains(blockState.getMaterial())) {
            return true;
        }

        if (ToolType.AXE.equals(toolType) && axeMaterials.contains(blockState.getMaterial())) {
            return true;
        }

        if (ToolType.PICKAXE.equals(toolType) && pickaxeMaterials.contains(blockState.getMaterial())) {
            return true;
        }

        return toolType.equals(blockState.getHarvestTool());
    }

    public static ToolType getEffectiveTool(BlockState blockState) {
        ToolType tool = blockState.getHarvestTool();

        if (tool != null) {
            return tool;
        }

        if (cuttingHarvestBlocks.contains(blockState.getBlock())
                || cuttingDestroyMaterials.contains(blockState.getMaterial())
                || cuttingDestroyTags.stream().anyMatch(tag -> blockState.getBlock().isIn(tag))) {
            return ToolTypes.cut;
        }

        if (axeMaterials.contains(blockState.getMaterial())) {
            return ToolType.AXE;
        }

        if (pickaxeMaterials.contains(blockState.getMaterial())) {
            return ToolType.PICKAXE;
        }

        return null;
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack stack, final ItemStack book) {
        return false;
    }

    @Override
    public ItemStack onCraftConsume(ItemStack providerStack, ItemStack targetStack, PlayerEntity player,
            ToolType tool, int toolLevel, boolean consumeResources) {
        if (consumeResources) {
            applyDamage(toolLevel, providerStack, player);

            applyUsageEffects(player, providerStack, 10 + toolLevel * 5);
        }

        return super.onCraftConsume(providerStack, targetStack, player, tool, toolLevel, consumeResources);
    }

    @Override
    public ItemStack onActionConsume(ItemStack providerStack, ItemStack targetStack, PlayerEntity player,
            ToolType tool, int toolLevel, boolean consumeResources) {
        if (consumeResources) {
            applyDamage(toolLevel, providerStack, player);

            applyUsageEffects(player, providerStack, 4 + toolLevel * 3);
        }

        return super.onCraftConsume(providerStack, targetStack, player, tool, toolLevel, consumeResources);
    }
}
