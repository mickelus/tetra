package se.mickelus.tetra.effect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.effect.potion.EarthboundPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ItemEffectHandler {

    public static ItemEffectHandler instance;

    public ItemEffectHandler() {
        instance = this;
    }

    public static void applyHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        int bleedingLevel = getEffectLevel(itemStack, ItemEffect.bleeding);
        if (bleedingLevel > 0) {
            if (!CreatureAttribute.UNDEAD.equals(target.getCreatureAttribute())
                    && attacker.getRNG().nextFloat() < 0.3f) {
                target.addPotionEffect(new EffectInstance(BleedingPotionEffect.instance, 40, bleedingLevel));
            }
        }

        // todo: only trigger if target is standing on stone/earth/sand/gravel
        int earthbindLevel = getEffectLevel(itemStack, ItemEffect.earthbind);
        if (earthbindLevel > 0 && attacker.getRNG().nextFloat() < Math.max(0.1, 0.5 * ( 1 - target.getPosY()  / 128 ))) {
            target.addPotionEffect(new EffectInstance(EarthboundPotionEffect.instance, earthbindLevel * 20, 0, false, true));

            if (target.world instanceof ServerWorld) {
                BlockState blockState = target.world.getBlockState(new BlockPos(target.getPosX(), target.getPosY() - 1, target.getPosZ()));
                ((ServerWorld)target.world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState),
                        target.getPosX(), target.getPosY() + 0.1, target.getPosZ(),
                        16, 0, target.world.rand.nextGaussian() * 0.2, 0, 0.1);
            }
        }
    }

    private static int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        ModularItem item = (ModularItem) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    private static double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
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
                                applyHitEffects(itemStack, attacker, blocker);
                                EffectHelper.applyEnchantmentHitEffects(itemStack, attacker, blocker);

                                float knockbackFactor = 0.5f + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack);
                                attacker.applyKnockback(knockbackFactor * 0.5f,
                                        blocker.getPosX() - attacker.getPosX(), blocker.getPosZ() - attacker.getPosZ());
                            }
                        }
                    });
        }

        if ("arrow".equals(event.getSource().damageType)) {
            CastOptional.cast(event.getSource().getTrueSource(), LivingEntity.class)
                    .map(shooter -> Stream.of(shooter.getHeldItemMainhand(), shooter.getHeldItemOffhand()))
                    .orElseGet(Stream::empty)
                    .filter(itemStack -> itemStack.getItem() instanceof ModularBowItem)
                    .findFirst()
                    .ifPresent(itemStack -> {
                        ModularBowItem item = (ModularBowItem) itemStack.getItem();
                        item.tickHoningProgression((LivingEntity) event.getSource().getTrueSource(), itemStack, 2);
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
                        if (item.getAttributeValue(itemStack, Attributes.ARMOR) > 0 || item.getAttributeValue(itemStack, Attributes.ARMOR_TOUGHNESS) > 0) {
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
        Optional.ofNullable(event.getEntityLiving().getActivePotionEffect(EarthboundPotionEffect.instance))
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClickInput(InputEvent.ClickInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack()
                && !event.isCanceled()
                && mc.player.getHeldItemMainhand().getItem() instanceof ModularItem
                && mc.objectMouseOver != null
                && RayTraceResult.Type.MISS.equals(Minecraft.getInstance().objectMouseOver.getType())) {
            if (getEffectLevel(mc.player.getHeldItemMainhand(), ItemEffect.truesweep) > 0) {
                SweepingEffect.triggerTruesweep();
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Optional.of(event.getItemStack())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                .ifPresent(itemStack -> {
                    ItemModularHandheld item = (ItemModularHandheld) itemStack.getItem();
                    BlockPos pos = event.getPos();
                    World world = event.getWorld();
                    BlockState blockState = world.getBlockState(pos);
                    PlayerEntity breakingPlayer = event.getPlayer();

                    boolean didStrike = StrikingEffect.causeEffect(breakingPlayer, itemStack, item, world, pos, blockState);
                    if (didStrike) {
                        event.setCanceled(true);
                    }

                    if (!event.getWorld().isRemote) {
                        int critLevel = getEffectLevel(itemStack, ItemEffect.criticalStrike);
                        if (critLevel > 0) {
                            if (critBlock(world, breakingPlayer, pos, blockState, itemStack, critLevel)) {
                                event.setCanceled(true);
                            }
                        }

                        if (breakingPlayer.getCooledAttackStrength(0.5f) > 0.9 && getEffectLevel(itemStack, ItemEffect.truesweep) > 0) {
                            SweepingEffect.truesweep(itemStack, breakingPlayer);
                        }
                    }
                });
    }

    private boolean critBlock(World world, PlayerEntity breakingPlayer, BlockPos pos, BlockState blockState, ItemStack itemStack, int critLevel) {
        if (breakingPlayer.getRNG().nextFloat() < critLevel * 0.01
                && blockState.getBlockHardness(world, pos) > -1
                && itemStack.getItem().getDestroySpeed(itemStack, blockState) > 2 * blockState.getBlockHardness(world, pos)) {

            ToolType tool = blockState.getHarvestTool();
            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);

            if (( toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState) ) || itemStack.canHarvestBlock(blockState)) {
                EffectHelper.breakBlock(world, breakingPlayer, itemStack, pos, blockState, true);
                itemStack.damageItem(2, breakingPlayer, t -> {});

                ((ModularItem) itemStack.getItem()).tickProgression(breakingPlayer, itemStack, 1);

                if (breakingPlayer instanceof ServerPlayerEntity) {
                    EffectHelper.sendEventToPlayer((ServerPlayerEntity) breakingPlayer, 2001, pos, Block.getStateId(blockState));
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
                int reverbLevel = PropertyHelper.getPlayerEffectLevel(player, ItemEffect.enderReverb);
                if (reverbLevel > 0 && !player.isCreative()) {
                    double effectProbability = PropertyHelper.getPlayerEffectEfficiency(player, ItemEffect.enderReverb);
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onArrowNock(ArrowNockEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!event.hasAmmo() && player.getHeldItem(Hand.OFF_HAND).isEmpty()) {
            ItemStack itemStack = ToolbeltHelper.findToolbelt(player);
            if (!itemStack.isEmpty()) {
                QuiverInventory inventory = new QuiverInventory(itemStack);
                List<Collection<ItemEffect>> effects = inventory.getSlotEffects();
                int count = CastOptional.cast(event.getBow().getItem(), ModularItem.class)
                        .map(item -> getEffectLevel(event.getBow(), ItemEffect.multishot))
                        .filter(level -> level > 0)
                        .orElse(1);
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    if (effects.get(i).contains(ItemEffect.quickAccess) && !inventory.getStackInSlot(i).isEmpty()) {

                        player.setHeldItem(Hand.OFF_HAND, inventory.getStackInSlot(i).split(count));
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
