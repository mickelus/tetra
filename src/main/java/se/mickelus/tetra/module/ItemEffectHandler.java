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
                    BlockPos pos = event.getPos();
                    World world = event.getWorld();
                    IBlockState blockState = world.getBlockState(pos);
                    String tool = blockState.getBlock().getHarvestTool(blockState);

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
                            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, event.getEntityPlayer(), blockState);
                            if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState)) || event.getItemStack().canHarvestBlock(blockState)) {
                                world.playEvent(event.getEntityPlayer(), 2001, event.getPos(), Block.getStateId(event.getWorld().getBlockState(event.getPos())));
                                if (!event.getWorld().isRemote) {
                                    boolean canRemove = blockState.getBlock().removedByPlayer(blockState, world, pos, event.getEntityPlayer(), true);
                                    if (canRemove) {
                                        blockState.getBlock().onBlockDestroyedByPlayer(world, pos, blockState);
                                        blockState.getBlock().harvestBlock(world, event.getEntityPlayer(), pos, blockState, world.getTileEntity(pos), itemStack);
                                    }
                                }
                            }
                        }
                        event.setCanceled(true);
                        event.getEntityPlayer().resetCooldown();
                    }
                });
    }
}
