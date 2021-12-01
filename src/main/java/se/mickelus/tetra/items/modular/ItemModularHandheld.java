package se.mickelus.tetra.items.modular;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.effect.*;
import se.mickelus.tetra.effect.howling.HowlingEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
@ParametersAreNonnullByDefault
public class ItemModularHandheld extends ModularItem {

    /**
     * Below are lists of blocks, materials and tags that describe what different tools can harvest and efficiently destroy. Note that these
     * are copies of what the vanilla tool counterparts explicitly state that they can destroy and harvest, some blocks (and required tiers)
     * are not listed here as that's part of that block's implementation.
     */

    private static final Set<Material> hoeBonusMaterials = Sets.newHashSet(Material.PLANT, Material.REPLACEABLE_PLANT, Material.CORAL);

    private static final Set<Material> axeMaterials = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.VEGETABLE);
    private static final Set<Material> pickaxeMaterials = Sets.newHashSet(Material.METAL, Material.HEAVY_METAL, Material.STONE);

    // copy of hardcoded values in SwordItem, materials & tag that it explicitly state it can efficiently DESTROY
    private static final Set<Material> cuttingDestroyMaterials = Sets.newHashSet(Material.PLANT, Material.REPLACEABLE_PLANT, Material.CORAL, Material.VEGETABLE, Material.WEB, Material.BAMBOO);
    private static final Set<Tag.Named<Block>> cuttingDestroyTags = Sets.newHashSet(BlockTags.LEAVES);

    // copy of hardcoded values in SwordItem, blocks that the sword explicitly state it can efficiently HARVEST
    private static final Set<Block> cuttingHarvestBlocks = Sets.newHashSet(Blocks.COBWEB);

    public static final ResourceLocation nailedTag = new ResourceLocation("tetra:nailed");

    // the base amount of damage the item should take after destroying a block
    protected int blockDestroyDamage = 1;

    // the base amount of damage the item should take after hitting an entity
    protected int entityHitDamage = 1;

    // if the blocking level exceeds this value the item has an infinite blocking duration
    public static final int blockingDurationLimit = 16;

    static final ChargedAbilityEffect[] abilities = new ChargedAbilityEffect[] {
            ExecuteEffect.instance,
            LungeEffect.instance,
            SlamEffect.instance,
            PunctureEffect.instance,
            OverpowerEffect.instance,
            ReapEffect.instance,
            PryChargedEffect.instance
    };

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
    public boolean isDamageable(ItemStack stack) {
        return stack.hasTag();
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(world, pos) > 0) {
            applyDamage(blockDestroyDamage, itemStack, entity);

            if (!isBroken(itemStack)) {
                applyUsageEffects(entity, itemStack, 1);
            }
        }

        applyBreakEffects(itemStack, world, state, pos, entity);

        if (!world.isClientSide && !isBroken(itemStack)) {
            if (getEffectLevel(itemStack, ItemEffect.piercingHarvest) > 0) {
                PiercingEffect.pierceBlocks(this, itemStack, getEffectLevel(itemStack, ItemEffect.piercing), (ServerLevel) world, state, pos, entity);
            }


            int extractorLevel = getEffectLevel(itemStack, ItemEffect.extractor);
            if (extractorLevel > 0) {
                ExtractorEffect.breakBlocks(this, itemStack, extractorLevel, (ServerLevel) world, state, pos, entity);
            }

            CritEffect.onBlockBreak(entity);
        }

        return true;
    }

    public void applyBreakEffects(ItemStack itemStack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!world.isClientSide) {
            int intuitLevel = getEffectLevel(itemStack, ItemEffect.intuit);
            if (intuitLevel > 0) {
                int xp = state.getExpDrop(world, pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, itemStack),
                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack));
                if (xp > 0) {
                    tickHoningProgression(entity, itemStack, xp);
                }
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        applyDamage(entityHitDamage, itemStack, attacker);

        if (!isBroken(itemStack)) {
            float attackStrength = CastOptional.cast(attacker, Player.class)
                    .map(EffectHelper::getCooledAttackStrength)
                    .orElse(1f);

            if (attackStrength > 0.9) {
                int sweepingLevel = getEffectLevel(itemStack, ItemEffect.sweeping);
                if (sweepingLevel > 0 && attacker.isOnGround() && !EffectHelper.getSprinting(attacker)) {
                    SweepingEffect.sweepAttack(itemStack, target, attacker, sweepingLevel);
                }

                int howlingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.howling);
                if (howlingLevel > 0) {
                    HowlingEffect.trigger(itemStack, attacker, howlingLevel);
                }

                ItemEffectHandler.applyHitEffects(itemStack, target, attacker);
                applyPositiveUsageEffects(attacker, itemStack, 1);
            }

            applyNegativeUsageEffects(attacker, itemStack, 1);
        }

        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        ItemStack itemStack = player.getItemInHand(hand);

        BlockState blockState = world.getBlockState(pos);

        if (isBroken(itemStack)) {
            return InteractionResult.PASS;
        }

        boolean canChannel = getUseDuration(itemStack) > 0;
        if (!canChannel || player.isCrouching()) {
            ToolData toolData = getToolData(itemStack);
            Collection<ToolAction> tools = toolData.getValues().stream()
                    .filter(tool -> toolData.getLevel(tool) > 0)
                    .sorted(player.isCrouching() ? Comparator.comparing(ToolAction::getName).reversed() : Comparator.comparing(ToolAction::getName))
                    .collect(Collectors.toList());

            for (ToolAction tool: tools) {
                BlockState block = blockState.getToolModifiedState(world, pos, context.getPlayer(), context.getItemInHand(), tool);
                if (block != null) {
                    SoundEvent sound = Optional.ofNullable(getUseSound(tool))
                            .orElseGet(() -> blockState.getSoundType(world, pos, player).getHitSound());

                    world.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!world.isClientSide) {
                        world.setBlock(pos, block, 11);
                        applyDamage(blockDestroyDamage, context.getItemInHand(), player);
                        applyUsageEffects(player, itemStack, 2);
                    }

                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }

            int denailingLevel = getEffectLevel(itemStack, ItemEffect.denailing);
            if (denailingLevel > 0 && player.getAttackStrengthScale(0) > 0.9) {
                if (denailBlock(player, world, pos, hand, facing)) {
                    applyDamage(blockDestroyDamage, itemStack, player);
                    applyUsageEffects(player, itemStack, 2);
                    player.resetAttackStrengthTicker();
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
        }

        return super.useOn(context);
    }

    private SoundEvent getUseSound(ToolAction tool) {
        if      (tool == ToolAction.AXE)    return SoundEvents.AXE_STRIP;
        else if (tool == ToolAction.HOE)    return SoundEvents.HOE_TILL;
        else if (tool == ToolAction.SHOVEL) return SoundEvents.SHOVEL_FLATTEN;
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // pass success for channeled abilities
        if (getUseDuration(itemStack) > 0) {
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.getCooldowns().isOnCooldown(this) && !isBroken(itemStack)) {
            if (getUseDuration(itemStack) == 0 || player.isCrouching()) {
                int bashingLevel = getEffectLevel(itemStack, ItemEffect.bashing);
                if (bashingLevel > 0) {
                    bashEntity(itemStack, bashingLevel, player, target);

                    tickProgression(player, itemStack, 2);
                    applyDamage(2, itemStack, player);
                    return InteractionResult.sidedSuccess(player.level.isClientSide);
                }

                int pryLevel = getEffectLevel(itemStack, ItemEffect.pry);
                if (pryLevel > 0) {
                    PryEffect.perform(player, hand, this, itemStack, pryLevel, target);
                    return InteractionResult.sidedSuccess(player.level.isClientSide);
                }

                if (InteractionHand.OFF_HAND.equals(hand)) {
                    int jabLevel = getEffectLevel(itemStack, ItemEffect.jab);
                    if (jabLevel > 0) {
                        jabEntity(itemStack, jabLevel, player, target);

                        tickProgression(player, itemStack, 2);
                        applyDamage(2, itemStack, player);
                        return InteractionResult.sidedSuccess(player.level.isClientSide);
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    public boolean itemInteractionForEntitySecondary(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        int bashingLevel = getEffectLevel(itemStack, ItemEffect.bashing);
        if (bashingLevel > 0) {
            bashEntity(itemStack, bashingLevel, player, target);

            tickProgression(player, itemStack, 2);
            applyDamage(2, itemStack, player);
            return true;
        }

        return false;
    }

    /**
     * Helper for hitting entities with abilities
     */
    public AbilityUseResult hitEntity(ItemStack itemStack, Player player, LivingEntity target, double damageMultiplier,
            float knockbackBase, float knockbackMultiplier) {
        return hitEntity(itemStack, player, target, damageMultiplier, 0, knockbackBase, knockbackMultiplier);
    }

    /**
     * Helper for hitting entities with abilities
     */
    public AbilityUseResult hitEntity(ItemStack itemStack, Player player, LivingEntity target, double damageMultiplier, double damageBonus,
            float knockbackBase, float knockbackMultiplier) {
        float targetModifier = EnchantmentHelper.getDamageBonus(itemStack, target.getMobType());
        float critMultiplier = Optional.ofNullable(ForgeHooks.getCriticalHit(player, target, false, 1.5f))
                .map(CriticalHitEvent::getDamageModifier)
                .orElse(1f);

        double damage = (1 + getAbilityBaseDamage(itemStack) + targetModifier) * critMultiplier * damageMultiplier + damageBonus;

        boolean success = target.hurt(DamageSource.playerAttack(player), (float) damage);
        if (success) {
            // applies enchantment effects on both parties
            EnchantmentHelper.doPostHurtEffects(target, player);
            EffectHelper.applyEnchantmentHitEffects(itemStack, target, player);

            // tetra item effects
            ItemEffectHandler.applyHitEffects(itemStack, target, player);

            // knocks back the target based on effect level + knockback enchantment level
            float knockbackFactor = knockbackBase + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, itemStack);
            target.knockback(knockbackFactor * knockbackMultiplier,
                    player.getX() - target.getX(), player.getZ() - target.getZ());

            if (targetModifier > 1) {
                player.magicCrit(target);
                return AbilityUseResult.magicCrit;
            }

            if (critMultiplier > 1) {
                player.crit(target);
                return AbilityUseResult.crit;
            }

            return AbilityUseResult.hit;
        }

        return AbilityUseResult.fail;
    }

    public void jabEntity(ItemStack itemStack, int jabLevel, Player player, LivingEntity target) {
        AbilityUseResult result = hitEntity(itemStack, player, target, jabLevel / 100f, 0.5f, 0.2f);

        if (result == AbilityUseResult.crit) {
            player.getCommandSenderWorld().playSound(player, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1, 1.3f);
        } else {
            player.getCommandSenderWorld().playSound(player, target.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1, 1.3f);
        }

        player.getCooldowns().addCooldown(this, (int) Math.round(getCooldownBase(itemStack) * 20));
    }

    public void bashEntity(ItemStack itemStack, int bashingLevel, Player player, LivingEntity target) {
        AbilityUseResult result = hitEntity(itemStack, player, target, 1, bashingLevel + (player.isSprinting() ? 1 : 0), 0.5f);

        if (result != AbilityUseResult.fail) {
            // stuns the target if bash efficiency is > 0
            double stunDuration = getEffectEfficiency(itemStack, ItemEffect.bashing);
            if (stunDuration > 0) {
                target.addEffect(new MobEffectInstance(StunPotionEffect.instance, (int) Math.round(stunDuration * 20), 0, false, false));
            }

            player.getCommandSenderWorld().playSound(player, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1, 0.7f);
        } else {
            player.getCommandSenderWorld().playSound(player, target.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1, 0.7f);
        }

        player.getCooldowns().addCooldown(this, (int) Math.round(getCooldownBase(itemStack) * 20));
    }

    public void throwItem(Player player, ItemStack stack, int riptideLevel, float cooldownBase) {
        Level world = player.level;
        if (!world.isClientSide) {
            applyDamage(1, stack, player);
            applyUsageEffects(player, stack, 1);

            ThrownModularItemEntity projectileEntity = new ThrownModularItemEntity(world, player, stack);

            if (player.getAbilities().instabuild) {
                projectileEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            } else {
                player.getInventory().removeItem(stack);
            }

            projectileEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + (float)riptideLevel * 0.5F, 1.0F);
            world.addFreshEntity(projectileEntity);

            if (this instanceof ModularSingleHeadedItem) {
                world.playSound(null, projectileEntity, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else if (this instanceof ModularShieldItem) {
                world.playSound(null, projectileEntity, SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 1.0F, 2F);
            } else {
                world.playSound(null, projectileEntity, SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 0.7F);
            }
        }

        player.getCooldowns().addCooldown(this, Math.round(cooldownBase * 20));
    }

    public void causeRiptideEffect(Player player, int riptideLevel) {
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        float x = -Mth.sin(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
        float y = -Mth.sin(pitch * ((float)Math.PI / 180F));
        float z = Mth.cos(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));

        float velocityMultiplier = 3.0F * ((1.0F + riptideLevel) / 4.0F);

        // vanilla divides this by the length of the directional vector, but that should always be 1
        x = x * velocityMultiplier;
        y = y * velocityMultiplier;
        z = z * velocityMultiplier;
        player.push(x, y, z);
        player.startAutoSpinAttack(20);
        if (player.isOnGround()) {
            player.move(MoverType.SELF, new Vec3(0, 1.1999999, 0));
        }

        SoundEvent soundEvent;
        if (riptideLevel >= 3) {
            soundEvent = SoundEvents.TRIDENT_RIPTIDE_3;
        } else if (riptideLevel == 2) {
            soundEvent = SoundEvents.TRIDENT_RIPTIDE_2;
        } else {
            soundEvent = SoundEvents.TRIDENT_RIPTIDE_1;
        }
        player.level.playSound(null, player, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);

        player.awardStat(Stats.ITEM_USED.get(this));

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
    public boolean denailBlock(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!player.mayUseItemAt(pos.relative(facing), facing, itemStack)) {
            return false;
        }

        BlockState blockState = world.getBlockState(pos);
        if (canDenail(blockState)) {
            boolean success = EffectHelper.breakBlock(world, player, player.getItemInHand(hand), pos, blockState, true);
            if (success) {
                player.resetAttackStrengthTicker();
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
    public UseAnim getUseAnimation(ItemStack stack) {
        if (getEffectLevel(stack, ItemEffect.blocking) > 0) {
            return UseAnim.BLOCK;
        }

        if (getEffectLevel(stack, ItemEffect.throwable) > 0
                || EnchantmentHelper.getRiptide(stack) > 0) {
            return UseAnim.SPEAR;
        }

        ChargedAbilityEffect ability = getChargeableAbility(stack);
        if (ability != null) {
            return ability.getPose();
        }

        return super.getUseAnimation(stack);
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
                    .filter(e -> e.getUseItemRemainingTicks() > 0)
                    .filter(e -> itemStack.equals(e.getUseItem()))
                    .map(e -> (e.getUseItemRemainingTicks()) * 1f / getUseDuration(itemStack))
                    .orElse(0f);
        }

        return 0;
    }

    public boolean isThrowing(ItemStack itemStack, @Nullable LivingEntity entity) {
        return UseAnim.SPEAR.equals(getUseAnimation(itemStack)) && Optional.ofNullable(entity)
                .filter(e -> itemStack.equals(e.getUseItem()))
                .map(e -> e.getUseItemRemainingTicks() > 0)
                .orElse(false);
    }
    public boolean isBlocking(ItemStack itemStack, @Nullable LivingEntity entity) {
        return UseAnim.BLOCK.equals(getUseAnimation(itemStack)) && Optional.ofNullable(entity)
                .filter(e -> itemStack.equals(e.getUseItem()))
                .map(e -> e.getUseItemRemainingTicks() > 0)
                .orElse(false);
    }

    @Override
    public boolean isShield(ItemStack itemStack, @Nullable LivingEntity entity) {
        return getEffectLevel(itemStack, ItemEffect.blocking) > 0;
    }

    public void onShieldDisabled(Player player, ItemStack itemStack) {
        player.getCooldowns().addCooldown(this, (int) (getCooldownBase(itemStack) * 20 * 0.75));
    }

    @Override
    public boolean canDisableShield(ItemStack itemStack, ItemStack shieldStack, LivingEntity target, LivingEntity attacker) {
        return getEffectLevel(itemStack, ItemEffect.shieldbreaker) > 0;
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
                || EnchantmentHelper.getRiptide(itemStack) > 0
                || Arrays.stream(abilities).anyMatch(ability -> ability.isAvailable(this, itemStack))) {
            return 72000;
        }

        return 0;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level world, LivingEntity entity) {
        CastOptional.cast(entity, Player.class)
                .ifPresent(player -> {
                    int blockingLevel = getEffectLevel(itemStack, ItemEffect.blocking);
                    if (blockingLevel > 0) {
                        double blockingCooldown = getEffectEfficiency(itemStack, ItemEffect.blocking);
                        if (blockingCooldown > 0) {
                            player.getCooldowns().addCooldown(this, (int) Math.round(blockingCooldown * getCooldownBase(itemStack) * 20));
                        }

                        if (player.isCrouching() && world.isClientSide) {
                            onPlayerStoppedUsingSecondary(itemStack, world, entity, 0);
                        }
                    }
                });

        return super.finishUsingItem(itemStack, world, entity);
    }

    /**
     * Called when the player stops using/channeling an item, e.g. when throwing a trident.
     * @param itemStack
     * @param world
     * @param entityLiving
     * @param timeLeft
     */
    @Override
    public void releaseUsing(ItemStack itemStack, Level world, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player) {
            Player player = (Player) entityLiving;
            int ticksUsed = this.getUseDuration(itemStack) - timeLeft;

            double cooldownBase = getCooldownBase(itemStack);
            int blockingLevel = getEffectLevel(itemStack, ItemEffect.blocking);
            int throwingLevel = getEffectLevel(itemStack, ItemEffect.throwable);
            int riptideLevel = EnchantmentHelper.getRiptide(itemStack);

            if (blockingLevel > 0) {
                double blockingCooldown = getEffectEfficiency(itemStack, ItemEffect.blocking);
                if (blockingCooldown > 0) {
                    player.getCooldowns().addCooldown(this, (int) Math.round(blockingCooldown * cooldownBase * 20));
                }

                if (player.isCrouching()) {
                    if (ticksUsed >= 10 && riptideLevel > 0 && player.isInWaterOrRain()) {
                        causeRiptideEffect(player, riptideLevel);
                    } else if (ticksUsed >= 10 && throwingLevel > 0) {
                        throwItem(player, itemStack, riptideLevel, (float) cooldownBase);
                    } else if (world.isClientSide) {
                        onPlayerStoppedUsingSecondary(itemStack, world, entityLiving, timeLeft);
                    }
                }
            } else {
                if (riptideLevel > 0 && ticksUsed >= 10 && player.isInWaterOrRain()) {
                    causeRiptideEffect(player, riptideLevel);
                } else if (throwingLevel > 0 && ticksUsed >= 10) {
                    throwItem(player, itemStack, riptideLevel, (float) cooldownBase);
                }

                if (world.isClientSide) {
                    triggerChargedAbility(itemStack, world, entityLiving, ticksUsed);
                }
            }
        }
    }

    public ChargedAbilityEffect getChargeableAbility(ItemStack itemStack) {
        return Arrays.stream(abilities)
                .filter(ability -> ability.canCharge(this, itemStack))
                .findFirst()
                .orElse(null);
    }

    @OnlyIn(Dist.CLIENT)
    public void triggerChargedAbility(ItemStack itemStack, Level world, LivingEntity entity, int ticksUsed) {
        if (entity instanceof Player) {
            HitResult rayTrace = Minecraft.getInstance().hitResult;

            LivingEntity target = Optional.ofNullable(rayTrace)
                    .filter(rayTraceResult -> rayTraceResult.getType() == HitResult.Type.ENTITY)
                    .map(rayTraceResult -> ((EntityHitResult) rayTraceResult).getEntity())
                    .flatMap(hitEntity -> CastOptional.cast(hitEntity, LivingEntity.class))
                    .orElse(null);

            BlockPos targetPos = Optional.ofNullable(rayTrace)
                    .filter(rayTraceResult -> rayTraceResult.getType() == HitResult.Type.BLOCK)
                    .map(rayTraceResult -> ((BlockHitResult) rayTraceResult).getBlockPos())
                    .orElse(null);

            Vec3 hitVec = Optional.ofNullable(rayTrace)
                    .map(HitResult::getLocation)
                    .orElse(null);

            InteractionHand activeHand = entity.getUsedItemHand();

            TetraMod.packetHandler.sendToServer(new ChargedAbilityPacket(target, targetPos, hitVec, activeHand, ticksUsed));

            handleChargedAbility((Player) entity, activeHand, target, targetPos, hitVec, ticksUsed);
        }
    }

    public static void handleChargedAbility(Player player, InteractionHand hand, @Nullable LivingEntity target, @Nullable BlockPos targetPos,
            @Nullable Vec3 hitVec, int ticksUsed) {
        ItemStack activeStack = player.getItemInHand(hand);

        if (!activeStack.isEmpty() && activeStack.getItem() instanceof ItemModularHandheld) {
            ItemModularHandheld item = (ItemModularHandheld) activeStack.getItem();

            Arrays.stream(abilities)
                    .filter(ability -> ability.canPerform(player, item, activeStack, target, targetPos, ticksUsed))
                    .findFirst()
                    .ifPresent(ability -> ability.perform(player, hand, item, activeStack, target, targetPos, hitVec, ticksUsed));

            player.stopUsingItem();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onPlayerStoppedUsingSecondary(ItemStack itemStack, Level world, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player) {
            LivingEntity target = Optional.ofNullable(Minecraft.getInstance().hitResult)
                    .filter(rayTraceResult -> rayTraceResult.getType() == HitResult.Type.ENTITY)
                    .map(rayTraceResult -> ((EntityHitResult)rayTraceResult).getEntity())
                    .flatMap(hitEntity -> CastOptional.cast(hitEntity, LivingEntity.class))
                    .orElse(null);

            InteractionHand activeHand = entity.getUsedItemHand();

            TetraMod.packetHandler.sendToServer(new SecondaryAbilityPacket(target, activeHand));

            handleSecondaryAbility((Player) entity, activeHand, target);
        }
    }

    public static void handleSecondaryAbility(Player player, InteractionHand hand, LivingEntity target) {
        ItemStack activeStack = player.getItemInHand(hand);

        if (!activeStack.isEmpty() && activeStack.getItem() instanceof ItemModularHandheld) {
            ItemModularHandheld item = (ItemModularHandheld) activeStack.getItem();

            if (target != null) {
                item.itemInteractionForEntitySecondary(activeStack, player, target, hand);

                player.stopUsingItem();
                player.getCooldowns().addCooldown(item, (int) Math.round(item.getCooldownBase(activeStack) * 20 * 1.5));
                player.swing(hand);
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        EffectHelper.setCooledAttackStrength(player, player.getAttackStrengthScale(0.5f));
        EffectHelper.setSprinting(player, player.isSprinting());
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return AttributeHelper.emptyMap;
        }

        if (slot == EquipmentSlot.MAINHAND) {
            return getAttributeModifiersCached(itemStack);
        }

        if (slot == EquipmentSlot.OFFHAND) {
            return getAttributeModifiersCached(itemStack).entries().stream()
                    .filter(entry -> entry.getKey().equals(Attributes.ARMOR) || entry.getKey().equals(Attributes.ARMOR_TOUGHNESS))
                    .collect(Multimaps.toMultimap(Map.Entry::getKey, Map.Entry::getValue, ArrayListMultimap::create));
        }

        return AttributeHelper.emptyMap;
    }

    public double getAbilityBaseDamage(ItemStack itemStack) {
        // +1 so that this equals the base damage of the item, including the players base attack damage
        return getAttributeValue(itemStack, Attributes.ATTACK_DAMAGE) + 1;
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
            int integrityCost = IModularItem.getIntegrityCost(itemStack);

            return getCounterWeightBonus(counterWeightLevel, integrityCost);
        }
        return 0;
    }

    public static double getCounterWeightBonus(int counterWeightLevel, int integrityCost) {
        return Math.max(0, 0.15 - Math.abs(counterWeightLevel - integrityCost) * 0.05);
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
    public Set<ToolAction> getToolTypes(ItemStack stack) {
        if (!isBroken(stack)) {
            return getTools(stack);
        }
        return Collections.emptySet();
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolAction tool, @Nullable Player player, @Nullable BlockState blockState) {
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
        if (!state.requiresCorrectToolForDrops()) {
            return true;
        }

        ToolAction requiredTool = state.getHarvestTool();
        if (requiredTool == null) {
            requiredTool = getEffectiveTool(state);
        }

        return requiredTool != null && getHarvestLevel(stack, requiredTool, null, state) >= Math.max(state.getHarvestLevel(), 0);
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if (!isBroken(itemStack)) {
            ToolAction tool = getEffectiveTool(blockState);
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

    public static boolean isToolEffective(ToolAction toolType, BlockState blockState) {
        if (ToolTypes.cut.equals(toolType)
                && (cuttingHarvestBlocks.contains(blockState.getBlock())
                    || cuttingDestroyMaterials.contains(blockState.getMaterial())
                    || cuttingDestroyTags.stream().anyMatch(tag -> blockState.getBlock().is(tag)))) {
            return true;
        }

        if (ToolAction.HOE.equals(toolType) && hoeBonusMaterials.contains(blockState.getMaterial())) {
            return true;
        }

        if (ToolAction.AXE.equals(toolType) && axeMaterials.contains(blockState.getMaterial())) {
            return true;
        }

        if (ToolAction.PICKAXE.equals(toolType) && pickaxeMaterials.contains(blockState.getMaterial())) {
            return true;
        }

        return toolType.equals(blockState.getHarvestTool());
    }

    public static ToolAction getEffectiveTool(BlockState blockState) {
        ToolAction tool = blockState.getHarvestTool();

        if (tool != null) {
            return tool;
        }

        if (cuttingHarvestBlocks.contains(blockState.getBlock())
                || cuttingDestroyMaterials.contains(blockState.getMaterial())
                || cuttingDestroyTags.stream().anyMatch(tag -> blockState.getBlock().is(tag))) {
            return ToolTypes.cut;
        }

        if (axeMaterials.contains(blockState.getMaterial())) {
            return ToolAction.AXE;
        }

        if (pickaxeMaterials.contains(blockState.getMaterial())) {
            return ToolAction.PICKAXE;
        }

        return null;
    }

    @Override
    public ItemStack onCraftConsume(ItemStack providerStack, ItemStack targetStack, Player player,
            ToolAction tool, int toolLevel, boolean consumeResources) {
        if (consumeResources) {
            applyDamage(toolLevel, providerStack, player);

            applyUsageEffects(player, providerStack, 10 + toolLevel * 5);
        }

        return super.onCraftConsume(providerStack, targetStack, player, tool, toolLevel, consumeResources);
    }

    @Override
    public ItemStack onActionConsume(ItemStack providerStack, ItemStack targetStack, Player player,
            ToolAction tool, int toolLevel, boolean consumeResources) {
        if (consumeResources) {
            applyDamage(toolLevel, providerStack, player);

            applyUsageEffects(player, providerStack, 4 + toolLevel * 3);
        }

        return super.onCraftConsume(providerStack, targetStack, player, tool, toolLevel, consumeResources);
    }
}
