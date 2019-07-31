package se.mickelus.tetra.module;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuiver;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
        ItemModular item = (ItemModular) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    @SubscribeEvent
    public void onLootingLevel(LootingLevelEvent event) {
        Optional.ofNullable(event.getDamageSource().getTrueSource())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    event.setLootingLevel(getEffectLevel(itemStack, ItemEffect.looting) + event.getLootingLevel());
                });
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Optional.ofNullable(event.getSource().getTrueSource())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    int quickStrikeLevel = getEffectLevel(itemStack, ItemEffect.quickStrike);
                    if (quickStrikeLevel > 0) {
                        float maxDamage = (float) ((EntityLivingBase) event.getSource().getTrueSource())
                                .getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                        float multiplier = quickStrikeLevel * 0.05f + 0.2f;

                        if (event.getAmount() <  multiplier * maxDamage) {
                            event.setAmount(multiplier * maxDamage);
                        }
                    }

                    if (EnumCreatureAttribute.UNDEAD.equals(event.getEntityLiving().getCreatureAttribute())) {
                        event.setAmount(event.getAmount() + getEffectLevel(itemStack, ItemEffect.smite) * 2.5f);
                    }

                    if (EnumCreatureAttribute.ARTHROPOD.equals(event.getEntityLiving().getCreatureAttribute())) {
                        event.setAmount(event.getAmount() + getEffectLevel(itemStack, ItemEffect.arthropod) * 2.5f);
                    }
                });
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        Optional.ofNullable(event.getSource().getTrueSource())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
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
    public void onCriticalHit(CriticalHitEvent event) {
        Optional.ofNullable(event.getEntityLiving())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    int backstabLevel = getEffectLevel(itemStack, ItemEffect.backstab);
                    if (backstabLevel > 0 && event.getTarget() instanceof EntityLivingBase) {
                        EntityLivingBase attacker = event.getEntityLiving();
                        EntityLivingBase target = (EntityLivingBase) event.getTarget();
                        if (180 - Math.abs(Math.abs(attacker.rotationYawHead - target.rotationYawHead) % 360 - 180) < 60) {
                            event.setDamageModifier(Math.max(1.25f + 0.25f * backstabLevel, event.getDamageModifier()));
                            event.setResult(Event.Result.ALLOW);
                        }
                    }
                });
    }

    @SubscribeEvent
    public void onPlayerPickupXp(PlayerPickupXpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        Stream.concat(
                player.inventory.mainInventory.stream(),
                player.inventory.offHandInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .filter(ItemStack::isItemDamaged)
                .filter(itemStack -> getEffectLevel(itemStack, ItemEffect.mending) > 0)
                .findAny()
                .ifPresent(itemStack -> {
                    int multiplier = getEffectLevel(itemStack, ItemEffect.mending) + 1;
                    EntityXPOrb orb = event.getOrb();
                    int durabilityGain = Math.min(orb.xpValue * multiplier, itemStack.getItemDamage());
                    orb.xpValue -= durabilityGain / multiplier;
                    itemStack.setItemDamage(itemStack.getItemDamage() - durabilityGain);

                    if (orb.xpValue <= 0) {
                        orb.xpValue = 0;
                        player.xpCooldown = 2;
                        player.onItemPickup(orb, 1);
                        orb.setDead();
                        event.setCanceled(true);
                    }
                });
    }

    @SubscribeEvent
    public void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Optional.ofNullable(event.getHarvester())
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    IBlockState state = event.getState();
                    if (!event.isSilkTouching()) {
                        int fortuneLevel = getEffectLevel(itemStack, ItemEffect.fortune);
                        if (fortuneLevel > 0) {
                            event.getDrops().clear();
                            // calling the new getDrops method directly cause some mod compatibility issues
                            // state.getBlock().getDrops(list, event.getWorld(), event.getPos(), state, fortuneLevel);
                            List<ItemStack> list = state.getBlock().getDrops(event.getWorld(), event.getPos(), state, fortuneLevel);
                            event.getDrops().addAll(list);
                        }
                    }
                });
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

        Optional.of(event.getItemStack())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    int strikingLevel = 0;
                    int sweepingLevel = getEffectLevel(itemStack, ItemEffect.sweepingStrike);
                    BlockPos pos = event.getPos();
                    World world = event.getWorld();
                    IBlockState blockState = world.getBlockState(pos);
                    String tool = ItemModularHandheld.getEffectiveTool(blockState);
                    EntityPlayer breakingPlayer = event.getEntityPlayer();

                    if (tool != null) {
                        switch (tool) {
                            case "axe":
                                strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingAxe);
                                break;
                            case "pickaxe":
                                strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingPickaxe);
                                break;
                            case "cut":
                                strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingCut);
                                break;
                            case "shovel":
                                strikingLevel = getEffectLevel(itemStack, ItemEffect.strikingShovel);
                                break;
                        }
                    }

                    if (strikingLevel > 0) {
                        if (breakingPlayer.getCooledAttackStrength(0) > 0.9) {
                            if (sweepingLevel > 0) {
                                breakBlocksAround(world, breakingPlayer, itemStack, pos, tool, sweepingLevel);
                            } else {
                                int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);
                                if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                                        || itemStack.canHarvestBlock(blockState)) {
                                    world.playEvent(breakingPlayer, 2001, pos, Block.getStateId(blockState));
                                    breakBlock(world, breakingPlayer, itemStack, pos, blockState);
                                }
                            }
                            itemStack.damageItem(2, breakingPlayer);
                        }
                        event.setCanceled(true);
                        breakingPlayer.resetCooldown();
                    }
                });
    }

    @SubscribeEvent
    public void onEnderTeleport(EnderTeleportEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            World world = event.getEntity().getEntityWorld();
            AxisAlignedBB aabb = new AxisAlignedBB(event.getTargetX() - 24, event.getTargetY() - 24, event.getTargetZ() - 24,
                    event.getTargetX() + 24, event.getTargetY() + 24, event.getTargetZ() + 24);

            event.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, aabb).forEach(player -> {
                int reverbLevel = CapabilityHelper.getPlayerEffectLevel(player, ItemEffect.enderReverb);
                if (reverbLevel > 0) {
                    double effectProbability = CapabilityHelper.getPlayerEffectEfficiency(player, ItemEffect.enderReverb);
                    if (effectProbability > 0) {
                        if (player.getRNG().nextDouble() < effectProbability * 2) {
                            player.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 40 * reverbLevel));
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
     * @return True if the player was allowed to break the block, otherwise false
     */
    public static boolean breakBlock(World world, EntityPlayer breakingPlayer, ItemStack toolStack, BlockPos pos, IBlockState blockState) {
        boolean canRemove = blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, true);
        if (canRemove && !world.isRemote) {
            blockState.getBlock().onBlockDestroyedByPlayer(world, pos, blockState);
            blockState.getBlock().harvestBlock(world, breakingPlayer, pos, blockState, world.getTileEntity(pos), toolStack);
        }

        return canRemove;
    }

    /**
     * Breaks several blocks around the given blockpos.
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param originPos the position which to break blocks around
     * @param tool a string representation used to break the center block, the tool required to break nearby blocks has
     *             to match this
     * @param sweepingLevel the level of the sweeping effect on the toolStack
     */
    private void breakBlocksAround(World world, EntityPlayer breakingPlayer, ItemStack toolStack, BlockPos originPos,
                                   String tool, int sweepingLevel) {
        if (world.isRemote) {
            return;
        }

        EnumFacing facing = breakingPlayer.getHorizontalFacing();
        final int strikeCounter = getStrikeCounter(breakingPlayer.getUniqueID());
        final boolean alternate = strikeCounter % 2 == 0;

        ItemModularHandheld.spawnSweepParticles(world, originPos.getX(), originPos.getY() + 0.5, originPos.getZ(), 0, 0);

        Arrays.stream((strikeCounter / 2) % 2 == 0 ? sweep1 : sweep2)
                .map(pos -> {
                    if (alternate) {
                        return new BlockPos(-pos.getX(), pos.getY(), pos.getZ());
                    }
                    return pos;
                })
                .map(pos -> rotatePos(pos, facing))
                .map(originPos::add)
                .forEachOrdered(pos -> {
                    IBlockState blockState = world.getBlockState(pos);

                    // make sure that only blocks which require the same tool are broken
                    if (tool.equals(ItemModularHandheld.getEffectiveTool(blockState))) {

                        // check that the tool level is high enough and break the block
                        int toolLevel = toolStack.getItem().getHarvestLevel(toolStack, tool, breakingPlayer, blockState);
                        if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                                || toolStack.canHarvestBlock(blockState)) {
                            world.playEvent(2001, pos, Block.getStateId(blockState));
                            breakBlock(world, breakingPlayer, toolStack, pos, blockState);
                        }
                    }

                });
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

    private BlockPos rotatePos(BlockPos pos, EnumFacing facing) {
        switch (facing) {
            default:
            case SOUTH:
                return pos;
            case WEST:
                return new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case NORTH:
                return new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case EAST:
                return new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onArrowNock(ArrowNockEvent event) {
        if (!event.hasAmmo() && event.getEntityPlayer().getHeldItem(EnumHand.OFF_HAND).isEmpty()) {
            ItemStack itemStack = UtilToolbelt.findToolbelt(event.getEntityPlayer());
            if (!itemStack.isEmpty()) {
                InventoryQuiver inventory = new InventoryQuiver(itemStack);
                List<Collection<ItemEffect>> effects = inventory.getSlotEffects();
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    if (effects.get(i).contains(ItemEffect.quickAccess) && !inventory.getStackInSlot(i).isEmpty()) {
                        event.getEntityPlayer().setHeldItem(EnumHand.OFF_HAND, inventory.getStackInSlot(i).splitStack(1));
                        event.getEntityPlayer().setActiveHand(event.getHand());
                        inventory.markDirty();

                        event.setAction(new ActionResult<>(EnumActionResult.SUCCESS, event.getBow()));
                        return;
                    }
                }
            }
        }
    }
}
