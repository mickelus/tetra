package se.mickelus.tetra.module;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.mickelus.tetra.ReflectionHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ItemEffectHandler {

    private Cache<Block, Method> silkMethodCache;

    public static ItemEffectHandler instance;

    public ItemEffectHandler() {
         silkMethodCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
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
                        double maxDamage = ItemModularHandheld.getDamageModifierStatic(itemStack);

                        if (event.getAmount() < quickStrikeLevel * 0.05 * maxDamage) {
                            event.setAmount((float) (quickStrikeLevel * 0.05f * maxDamage));
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
                    if (penetratingLevel > 0 && event.getAmount() < penetratingLevel * 2) {
                        event.setAmount(penetratingLevel * 2);
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
                    EntityXPOrb orb = event.getOrb();
                    int i = Math.min(orb.xpValue * 2, itemStack.getItemDamage());
                    orb.xpValue -= i * 2;
                    itemStack.setItemDamage(itemStack.getItemDamage() - i);

                    if (orb.xpValue == 0) {
                        player.xpCooldown = 2;
                        player.onItemPickup(event.getOrb(), 1);
                        orb.setDead();
                        event.setCanceled(true);
                    }
                });
    }

    @SubscribeEvent
    public void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
        Optional.ofNullable(event.getHarvester())
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    int silkTouchLevel = getEffectLevel(itemStack, ItemEffect.silkTouch);
                    IBlockState state = event.getState();
                    if (silkTouchLevel > 0
                            && state.getBlock().canSilkHarvest(event.getWorld(), event.getPos(), state, event.getHarvester())) {
                        try {
                            ItemStack silkDrop = (ItemStack) silkMethodCache.get(state.getBlock(), () -> {
                                return ReflectionHelper.findMethod(state.getBlock().getClass(),
                                        "getSilkTouchDrop","func_180643_i", IBlockState.class);
                            })
                                    .invoke(state.getBlock(), state);

                            event.getDrops().clear();
                            event.getDrops().add(silkDrop);
                        } catch (IllegalAccessException | InvocationTargetException | ExecutionException e) {
                            e.printStackTrace();
                        }

                    } else {
                        int fortuneLevel = getEffectLevel(itemStack, ItemEffect.fortune);
                        if (fortuneLevel > 0) {
                            NonNullList<ItemStack> list = NonNullList.create();
                            event.getDrops().clear();
                            state.getBlock().getDrops(list, event.getWorld(), event.getPos(), state, fortuneLevel);
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
                        if (event.getEntityPlayer().getCooledAttackStrength(0) > 0.9) {

                            if (sweepingLevel > 0) {
                                breakBlocksAround(world, event.getEntityPlayer(), itemStack, pos, tool, sweepingLevel);
                            } else {
                                int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, event.getEntityPlayer(), blockState);
                                if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                                        || itemStack.canHarvestBlock(blockState)) {
                                    breakBlock(world, event.getEntityPlayer(), itemStack, pos, blockState);
                                }
                            }
                        }
                        event.setCanceled(true);
                        event.getEntityPlayer().resetCooldown();
                    }
                });
    }

    /**
     * Break a block in the world, as a player. Based on how players break blocks in vanilla.
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param pos the position which to break blocks around
     * @param blockState the state of the block that is to broken
     */
    private void breakBlock(World world, EntityPlayer breakingPlayer, ItemStack toolStack, BlockPos pos, IBlockState blockState) {
        // todo: perhaps we don't want to play the event here to avoid noise when breaking blocks with sweeping
        world.playEvent(breakingPlayer, 2001, pos, Block.getStateId(blockState));
        if (!world.isRemote) {
            boolean canRemove = blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, true);
            if (canRemove) {
                blockState.getBlock().onBlockDestroyedByPlayer(world, pos, blockState);
                blockState.getBlock().harvestBlock(world, breakingPlayer, pos, blockState, world.getTileEntity(pos), toolStack);
            }
        }
    }

    /**
     * Breaks several blocks around the given blockpos.
     * todo: currently breaks all in a 3x3 area, improve to use better shapes and depend on sweeping level
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param pos the position which to break blocks around
     * @param tool a string representation used to break the center block, the tool required to break nearby blocks has
     *             to match this
     * @param sweepingLevel the level of the sweeping effect on the toolStack
     */
    private void breakBlocksAround(World world, EntityPlayer breakingPlayer, ItemStack toolStack, BlockPos pos,
                                   String tool, int sweepingLevel) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos offsetPos = pos.add(x, y, z);
                    IBlockState blockState = world.getBlockState(offsetPos);

                    // make sure that only blocks which require the same tool are broken
                    if (!tool.equals(ItemModularHandheld.getEffectiveTool(blockState))) {
                        continue;
                    }

                    // check that the tool level is high enough and break the block
                    int toolLevel = toolStack.getItem().getHarvestLevel(toolStack, tool, breakingPlayer, blockState);
                    if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                            || toolStack.canHarvestBlock(blockState)) {
                        breakBlock(world, breakingPlayer, toolStack, offsetPos, blockState);
                    }
                }
            }
        }
    }
}
