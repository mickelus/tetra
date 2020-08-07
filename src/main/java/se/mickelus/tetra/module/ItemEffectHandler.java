package se.mickelus.tetra.module;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.RotationHelper;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.effects.EarthboundEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemEffectHandler {

    private Cache<UUID, Integer> strikeCache;

    public static ItemEffectHandler instance;

    private static final BlockPos[] sweep1 = new BlockPos[] {
            new BlockPos(-2, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(2, 0, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 1),
            new BlockPos(-3, 0, -1),
            new BlockPos(-2, 0, -1),
            new BlockPos(-1, 0, -1),
            new BlockPos(0, 0, -1),
            new BlockPos(3, 0, -1),
            new BlockPos(-3, 0, -2),
            new BlockPos(-2, 0, -2),
            new BlockPos(-1, 0, -2),
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(-2, 1, -1),
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(-2, -1, -1),
            new BlockPos(-1, -1, -1),
    };

    private static final BlockPos[] sweep2 = new BlockPos[] {
            new BlockPos(-2, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(2, 0, 0),
            new BlockPos(3, 0, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 1),
            new BlockPos(2, 0, 1),
            new BlockPos(-2, 0, -1),
            new BlockPos(-1, 0, -1),
            new BlockPos(0, 0, -1),
            new BlockPos(4, 0, -1),
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(1, 1, 0),
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(1, -1, 0),
    };

    public ItemEffectHandler() {
        strikeCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        instance = this;
    }

    private int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        ModularItem item = (ModularItem) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    private double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        ModularItem item = (ModularItem) itemStack.getItem();
        return item.getEffectEfficiency(itemStack, effect);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public void onExperienceDrop(LivingExperienceDropEvent event) {
        Optional.ofNullable(event.getAttackingPlayer())
                .map(LivingEntity::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ModularItem)
                .ifPresent(itemStack -> {
                    int intuitLevel = getEffectLevel(itemStack, ItemEffect.intuit);
                    int xp = event.getDroppedExperience();
                    if (intuitLevel > 0 && xp > 0) {
                        ((ModularItem) itemStack.getItem()).tickHoningProgression(event.getAttackingPlayer(), itemStack, intuitLevel * xp);
                    }
                });
    }


    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!event.getSource().isUnblockable() && event.getEntityLiving().isActiveItemStackBlocking()) {
            Optional.ofNullable(event.getEntityLiving())
                    .map(LivingEntity::getActiveItemStack)
                    .filter(itemStack -> itemStack.getItem() instanceof ModularItem)
                    .ifPresent(itemStack -> {
                        ItemModularHandheld item = (ItemModularHandheld) itemStack.getItem();
                        LivingEntity blocker = event.getEntityLiving();
                        if (UseAction.BLOCK.equals(itemStack.getUseAction())) {
                            item.applyUsageEffects(blocker, itemStack, MathHelper.ceil(event.getAmount() / 2f));
                        }

                        if (event.getSource().getImmediateSource() instanceof LivingEntity) {
                            LivingEntity attacker = (LivingEntity) event.getSource().getImmediateSource();

                            if (item.getEffectLevel(itemStack, ItemEffect.blockingReflect) > attacker.getRNG().nextFloat() * 100) {
                                attacker.attackEntityFrom(new EntityDamageSource("thorns", blocker).setIsThornsDamage(),
                                        (float) (item.getAbilityBaseDamage(itemStack) * item.getEffectEfficiency(itemStack, ItemEffect.blockingReflect)));
                                item.applyHitEffects(itemStack, attacker, blocker);
                                ItemModularHandheld.applyEnchantmentHitEffects(itemStack, attacker, blocker);

                                float knockbackFactor = 0.5f + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack);
                                attacker.applyKnockback(knockbackFactor * 0.5f,
                                        blocker.getPosX() - attacker.getPosX(), blocker.getPosZ() - attacker.getPosZ());
                            }
                        }
                    });
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Optional.ofNullable(event.getSource().getTrueSource())
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .map(LivingEntity::getHeldItemMainhand)
                .filter(itemStack -> itemStack.getItem() instanceof ModularItem)
                .ifPresent(itemStack -> {
                    int quickStrikeLevel = getEffectLevel(itemStack, ItemEffect.quickStrike);
                    if (quickStrikeLevel > 0) {
                        float maxDamage = (float) ((LivingEntity) event.getSource().getTrueSource())
                                .getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                        float multiplier = quickStrikeLevel * 0.05f + 0.2f;

                        if (event.getAmount() <  multiplier * maxDamage) {
                            event.setAmount(multiplier * maxDamage);
                        }
                    }
                });

        if (!event.getSource().isUnblockable()) {
            Optional.ofNullable(event.getEntityLiving())
                    .map(entity -> Stream.of(entity.getHeldItemMainhand(), entity.getHeldItemOffhand()))
                    .orElseGet(Stream::empty)
                    .filter(itemStack -> !itemStack.isEmpty())
                    .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                    .forEach(itemStack -> {
                        ItemModularHandheld item = (ItemModularHandheld) itemStack.getItem();
                        if (item.getEffectLevel(itemStack, ItemEffect.armor) > 0 || item.getEffectLevel(itemStack, ItemEffect.toughness) > 0) {
                            int reducedAmount = (int) Math.ceil(event.getAmount() - CombatRules.getDamageAfterAbsorb(event.getAmount(),
                                    (float) event.getEntityLiving().getTotalArmorValue(),
                                    (float) event.getEntityLiving().getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()));
                            item.applyUsageEffects(event.getEntityLiving(), itemStack, reducedAmount);
                            item.applyDamage(reducedAmount, itemStack, event.getEntityLiving());
                        }
                    });
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        Optional.ofNullable(event.getSource().getTrueSource())
                .filter(entity -> entity instanceof PlayerEntity)
                .map(entity -> (LivingEntity) entity)
                .map(LivingEntity::getHeldItemMainhand)
                .filter(itemStack -> itemStack.getItem() instanceof ModularItem)
                .ifPresent(itemStack -> {
                    int penetratingLevel = getEffectLevel(itemStack, ItemEffect.armorPenetration);
                    if (penetratingLevel > 0 && event.getAmount() < penetratingLevel) {
                        event.setAmount(penetratingLevel);
                    }

                    int unarmoredBonusLevel = getEffectLevel(itemStack, ItemEffect.unarmoredDamage);
                    if (unarmoredBonusLevel > 0 && event.getEntityLiving().getTotalArmorValue() == 0) {
                        event.setAmount(event.getAmount()  + unarmoredBonusLevel);
                    }
                });
    }


    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        Optional.ofNullable(event.getEntityLiving().getActivePotionEffect(EarthboundEffect.instance))
                .ifPresent(effect -> event.getEntityLiving().setMotion(event.getEntityLiving().getMotion().mul(1, 0.5, 1)));
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Optional.ofNullable(event.getEntityLiving())
                .filter(entity -> entity instanceof PlayerEntity)
                .map(entity -> (LivingEntity) entity)
                .map(LivingEntity::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ModularItem)
                .ifPresent(itemStack -> {
                    int backstabLevel = getEffectLevel(itemStack, ItemEffect.backstab);
                    if (backstabLevel > 0 && event.getTarget() instanceof LivingEntity) {
                        LivingEntity attacker = event.getEntityLiving();
                        LivingEntity target = (LivingEntity) event.getTarget();
                        if (180 - Math.abs(Math.abs(attacker.rotationYawHead - target.rotationYawHead) % 360 - 180) < 60) {
                            event.setDamageModifier(Math.max(1.25f + 0.25f * backstabLevel, event.getDamageModifier()));
                            event.setResult(Event.Result.ALLOW);
                        }
                    }

                    int critLevel = getEffectLevel(itemStack, ItemEffect.criticalStrike);
                    if (critLevel > 0) {
                        if (event.getEntityLiving().getRNG().nextFloat() < critLevel * 0.01) {
                            event.setDamageModifier(Math.max((float) getEffectEfficiency(itemStack, ItemEffect.criticalStrike), event.getDamageModifier()));
                            event.setResult(Event.Result.ALLOW);
                        }
                    }
                });
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Optional.of(event.getItemStack())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                .ifPresent(itemStack -> {
                    ItemModularHandheld item = (ItemModularHandheld) itemStack.getItem();
                    int strikingLevel = 0;
                    BlockPos pos = event.getPos();
                    World world = event.getWorld();
                    BlockState blockState = world.getBlockState(pos);
                    PlayerEntity breakingPlayer = event.getPlayer();
                    ToolType tool = null;

                    // essentially checks if the item is effective in for each tool type, and checks if it can strike for that type
                    if (ItemModularHandheld.isToolEffective(ToolType.AXE, blockState)) {
                        strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingAxe);
                        if (strikingLevel > 0) {
                            tool = ToolType.AXE;
                        }
                    }

                    if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolType.PICKAXE, blockState)) {
                        strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingPickaxe);
                        if (strikingLevel > 0) {
                            tool = ToolType.PICKAXE;
                        }
                    }

                    if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolTypes.cut, blockState)) {
                        strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingCut);
                        if (strikingLevel > 0) {
                            tool = ToolTypes.cut;
                        }
                    }

                    if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolType.SHOVEL, blockState)) {
                        strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingShovel);
                        if (strikingLevel > 0) {
                            tool = ToolType.SHOVEL;
                        }
                    }

                    if (strikingLevel > 0) {
                        int sweepingLevel = getEffectLevel(itemStack, ItemEffect.sweepingStrike);
                        if (breakingPlayer.getCooledAttackStrength(0) > 0.9) {
                            if (sweepingLevel > 0) {
                                breakBlocksAround(world, breakingPlayer, itemStack, pos, tool, sweepingLevel);
                            } else {
                                int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);
                                if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                                        || itemStack.canHarvestBlock(blockState)) {
                                    breakBlock(world, breakingPlayer, itemStack, pos, blockState, true);
                                }
                            }

                            item.applyUsageEffects(breakingPlayer, itemStack, 1);
                            item.applyDamage(item.getBlockDestroyDamage(), itemStack, breakingPlayer);
                        }
                        event.setCanceled(true);
                        breakingPlayer.resetCooldown();
                    }

                    if (!event.getWorld().isRemote) {
                        int critLevel = getEffectLevel(itemStack, ItemEffect.criticalStrike);
                        if (critLevel > 0) {
                            if (critBlock(world, breakingPlayer, pos, blockState, itemStack, tool, critLevel)) {
                                event.setCanceled(true);
                            }
                        }
                    }
                });
    }

    private boolean critBlock(World world, PlayerEntity breakingPlayer, BlockPos pos, BlockState blockState, ItemStack itemStack,
            ToolType tool, int critLevel) {
        if (breakingPlayer.getRNG().nextFloat() < critLevel * 0.01
                && blockState.getBlockHardness(world, pos) > -1
                && itemStack.getItem().getDestroySpeed(itemStack, blockState) > 2 * blockState.getBlockHardness(world, pos)) {
            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);

            if (( toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState) ) || itemStack.canHarvestBlock(blockState)) {
                breakBlock(world, breakingPlayer, itemStack, pos, blockState, true);
                itemStack.damageItem(2, breakingPlayer, t -> {});

                ((ModularItem) itemStack.getItem()).tickProgression(breakingPlayer, itemStack, 1);

                if (breakingPlayer instanceof ServerPlayerEntity) {
                    sendEventToPlayer((ServerPlayerEntity) breakingPlayer, 2001, pos, Block.getStateId(blockState));
                }

                if (world instanceof ServerWorld) {
                    ((ServerWorld) world).spawnParticle(ParticleTypes.CRIT,
                            pos.getX() + 0.5, // world.rand.nextGaussian(),
                            pos.getY() + 0.5, // world.rand.nextGaussian(),
                            pos.getZ() + 0.5, // world.rand.nextGaussian(),
                            12,
                            (world.rand.nextDouble() * 2.0D - 1.0D) * 0.3D,
                            0.3D + world.rand.nextDouble() * 0.3D,
                            (world.rand.nextDouble() * 2.0D - 1.0D) * 0.3D,
                            0.3);
                }

                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onEnderTeleport(EnderTeleportEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            AxisAlignedBB aabb = new AxisAlignedBB(
                    event.getTargetX() - 24, event.getTargetY() - 24, event.getTargetZ() - 24,
                    event.getTargetX() + 24, event.getTargetY() + 24, event.getTargetZ() + 24);

            event.getEntity().getEntityWorld().getEntitiesWithinAABB(PlayerEntity.class, aabb).forEach(player -> {
                int reverbLevel = CapabilityHelper.getPlayerEffectLevel(player, ItemEffect.enderReverb);
                if (reverbLevel > 0) {
                    double effectProbability = CapabilityHelper.getPlayerEffectEfficiency(player, ItemEffect.enderReverb);
                    if (effectProbability > 0) {
                        if (player.getRNG().nextDouble() < effectProbability * 2) {
                            player.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
                            player.addPotionEffect(new EffectInstance(Effects.NAUSEA, 40 * reverbLevel));
                        }
                    }
                }
            });
        }
    }

    /**
     * Break a block in the world, as a player. Based on how players break blocks in vanilla.
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param pos the position which to break blocks around
     * @param blockState the state of the block that is to broken
     * @param harvest true if the player is ment to harvest the block, false if it should just magically disappear
     * @return True if the player was allowed to break the block, otherwise false
     */
    public static boolean breakBlock(World world, PlayerEntity breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState,
            boolean harvest) {
        if (!world.isRemote) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) breakingPlayer;
            GameType gameType = serverPlayer.interactionManager.getGameType();

            int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, serverPlayer, pos);

            if (exp != -1) {
                boolean canRemove = !toolStack.onBlockStartBreak(pos, breakingPlayer)
                        && !breakingPlayer.blockActionRestricted(world, pos, gameType)
                        && (!harvest || blockState.canHarvestBlock(world, pos, breakingPlayer))
                        && blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest, world.getFluidState(pos));

                if (canRemove) {
                    blockState.getBlock().onPlayerDestroy(world, pos, blockState);

                    if (harvest) {
                        blockState.getBlock().harvestBlock(world, breakingPlayer, pos, blockState, world.getTileEntity(pos), toolStack);

                        if (exp > 0) {
                            blockState.getBlock().dropXpOnBlockBreak(world, pos, exp);
                        }
                    }
                }
                return canRemove;
            }

            return false;
        } else {
            return blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest,
                    world.getFluidState(pos));
        }
    }

    /**
     * Breaks several blocks around the given blockpos.
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param originPos the position which to break blocks around
     * @param tool the type of tool used to break the center block, the tool required to break nearby blocks has to
     *             match this
     * @param sweepingLevel the level of the sweeping effect on the toolStack
     */
    private void breakBlocksAround(World world, PlayerEntity breakingPlayer, ItemStack toolStack, BlockPos originPos, ToolType tool,
            int sweepingLevel) {
        if (world.isRemote) {
            return;
        }

        Direction facing = breakingPlayer.getHorizontalFacing();
        final int strikeCounter = getStrikeCounter(breakingPlayer.getUniqueID());
        final boolean alternate = strikeCounter % 2 == 0;

        float efficiency = CastOptional.cast(toolStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getCapabilityEfficiency(toolStack, tool))
                .orElse(0f);

        breakingPlayer.spawnSweepParticles();

        List<BlockPos> positions = Arrays.stream((strikeCounter / 2) % 2 == 0 ? sweep1 : sweep2)
                .map(pos -> (alternate ? new BlockPos(-pos.getX(), pos.getY(), pos.getZ()) : pos))
                .map(pos -> RotationHelper.rotatePitch(pos, breakingPlayer.rotationPitch))
                .map(pos -> RotationHelper.rotateCardinal(pos, facing))
                .map(originPos::add)
                .collect(Collectors.toList());

        for (BlockPos pos : positions) {
            BlockState blockState = world.getBlockState(pos);

            // make sure that only blocks which require the same tool are broken
            if (ItemModularHandheld.isToolEffective(tool, blockState)) {

                // check that the tool level is high enough and break the block
                int toolLevel = toolStack.getItem().getHarvestLevel(toolStack, tool, breakingPlayer, blockState);
                if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                        || toolStack.canHarvestBlock(blockState)) {

                    // the break event has to be sent the player separately as it's sent to the others inside Block.onBlockHarvested
                    if (breakingPlayer instanceof ServerPlayerEntity) {
                        sendEventToPlayer((ServerPlayerEntity) breakingPlayer, 2001, pos, Block.getStateId(blockState));
                    }

                    // adds a fixed amount to make blocks like grass still "consume" some efficiency
                    efficiency -= blockState.getBlockHardness(world, pos) + 0.5;

                    breakBlock(world, breakingPlayer, toolStack, pos, blockState, true);
                } else {
                    break;
                }
            } else if (blockState.isSolid()) {
                efficiency -= blockState.getBlockHardness(world, pos);
            }

            if (efficiency <= 0) {
                break;
            }
        }
    }

    /**
     * Sends an event to a specific player.
     * @param player the player the event will be sent to
     * @param type an integer representation of the event
     * @param pos the position in which the event takes place
     * @param data an integer representation of event data (e.g. Block.getStateId)
     */
    public static void sendEventToPlayer(ServerPlayerEntity player, int type, BlockPos pos, int data) {
        player.connection.sendPacket(new SPlaySoundEventPacket(type, pos, data, false));
    }

    /**
     * Gets and increments counter for recurrent strike made by the given entity. Expires after a minute.
     * @param entityId The ID of the responsible entity
     * @return The number of recurrent strikes
     */
    private int getStrikeCounter(UUID entityId) {
        int counter = 0;
        try {
            counter = strikeCache.get(entityId, () -> 0);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        strikeCache.put(entityId, counter + 1);

        return counter;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onArrowNock(ArrowNockEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!event.hasAmmo() && player.getHeldItem(Hand.OFF_HAND).isEmpty()) {
            ItemStack itemStack = ToolbeltHelper.findToolbelt(player);
            if (!itemStack.isEmpty()) {
                QuiverInventory inventory = new QuiverInventory(itemStack);
                List<Collection<ItemEffect>> effects = inventory.getSlotEffects();
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    if (effects.get(i).contains(ItemEffect.quickAccess) && !inventory.getStackInSlot(i).isEmpty()) {
                        player.setHeldItem(Hand.OFF_HAND, inventory.getStackInSlot(i).split(1));
                        player.setActiveHand(event.getHand());
                        inventory.markDirty();

                        event.setAction(new ActionResult<>(ActionResultType.SUCCESS, event.getBow()));
                        return;
                    }
                }
            }
        }
    }
}
