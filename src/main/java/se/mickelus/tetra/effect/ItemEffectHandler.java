package se.mickelus.tetra.effect;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.effect.howling.HowlingEffect;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.effect.potion.EarthboundPotionEffect;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.IModularItem;
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
            if (!CreatureAttribute.UNDEAD.equals(target.getMobType())
                    && attacker.getRandom().nextFloat() < 0.3f) {
                target.addEffect(new EffectInstance(BleedingPotionEffect.instance, 40, bleedingLevel));
            }
        }

        int severLevel = getEffectLevel(itemStack, ItemEffect.severing);
        if (severLevel > 0) {
            SeveringEffect.perform(itemStack, severLevel, attacker, target);
        }

        // todo: only trigger if target is standing on stone/earth/sand/gravel
        int earthbindLevel = getEffectLevel(itemStack, ItemEffect.earthbind);
        if (earthbindLevel > 0 && attacker.getRandom().nextFloat() < Math.max(0.1, 0.5 * ( 1 - target.getY()  / 128 ))) {
            target.addEffect(new EffectInstance(EarthboundPotionEffect.instance, earthbindLevel * 20, 0, false, true));

            if (target.level instanceof ServerWorld) {
                BlockState blockState = target.level.getBlockState(new BlockPos(target.getX(), target.getY() - 1, target.getZ()));
                ((ServerWorld)target.level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, blockState),
                        target.getX(), target.getY() + 0.1, target.getZ(),
                        16, 0, target.level.random.nextGaussian() * 0.2, 0, 0.1);
            }
        }

        int stunLevel = getEffectLevel(itemStack, ItemEffect.stun);
        if (stunLevel > 0) {
            StunEffect.perform(itemStack, stunLevel, attacker, target);
        }
    }

    private static int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        return EffectHelper.getEffectLevel(itemStack, effect);
    }

    private static double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        return EffectHelper.getEffectEfficiency(itemStack, effect);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public void onExperienceDrop(LivingExperienceDropEvent event) {
        Optional.ofNullable(event.getAttackingPlayer())
                .map(LivingEntity::getMainHandItem)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .ifPresent(itemStack -> {
                    int intuitLevel = getEffectLevel(itemStack, ItemEffect.intuit);
                    int xp = event.getDroppedExperience();
                    if (intuitLevel > 0 && xp > 0) {
                        ((IModularItem) itemStack.getItem()).tickHoningProgression(event.getAttackingPlayer(), itemStack, intuitLevel * xp);
                    }
                });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!event.isCanceled()) {
            ComboPoints.onAttackEntity(event);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!event.getSource().isBypassArmor() && event.getEntityLiving().isBlocking()) {
            Optional.ofNullable(event.getEntityLiving())
                    .map(LivingEntity::getUseItem)
                    .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                    .ifPresent(itemStack -> {
                        ItemModularHandheld item = (ItemModularHandheld) itemStack.getItem();
                        LivingEntity blocker = event.getEntityLiving();
                        if (UseAction.BLOCK.equals(itemStack.getUseAnimation())) {
                            item.applyUsageEffects(blocker, itemStack, MathHelper.ceil(event.getAmount() / 2f));
                        }

                        if (event.getSource().getDirectEntity() instanceof LivingEntity) {
                            LivingEntity attacker = (LivingEntity) event.getSource().getDirectEntity();

                            if (item.getEffectLevel(itemStack, ItemEffect.blockingReflect) > attacker.getRandom().nextFloat() * 100) {
                                attacker.hurt(new EntityDamageSource("thorns", blocker).setThorns(),
                                        (float) (item.getAbilityBaseDamage(itemStack) * item.getEffectEfficiency(itemStack, ItemEffect.blockingReflect)));
                                applyHitEffects(itemStack, attacker, blocker);
                                EffectHelper.applyEnchantmentHitEffects(itemStack, attacker, blocker);

                                float knockbackFactor = 0.5f + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, itemStack);
                                attacker.knockback(knockbackFactor * 0.5f,
                                        blocker.getX() - attacker.getX(), blocker.getZ() - attacker.getZ());
                            }
                        }
                    });
        }

        if ("arrow".equals(event.getSource().msgId)) {
            CastOptional.cast(event.getSource().getEntity(), LivingEntity.class)
                    .map(shooter -> Stream.of(shooter.getMainHandItem(), shooter.getOffhandItem()))
                    .orElseGet(Stream::empty)
                    .filter(itemStack -> itemStack.getItem() instanceof ModularBowItem)
                    .findFirst()
                    .ifPresent(itemStack -> {
                        ModularBowItem item = (ModularBowItem) itemStack.getItem();
                        item.tickHoningProgression((LivingEntity) event.getSource().getEntity(), itemStack, 2);
                    });

        }

        RevengeTracker.onAttackEntity(event);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (TickEvent.Phase.START == event.phase) {
            LungeEffect.onPlayerTick(event.player);
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent.Arrow event) {
        HowlingEffect.deflectProjectile(event, event.getArrow(), event.getRayTraceResult());
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Optional.ofNullable(event.getSource().getEntity())
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .map(LivingEntity::getMainHandItem)
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .ifPresent(itemStack -> {
                    int quickStrikeLevel = getEffectLevel(itemStack, ItemEffect.quickStrike);
                    if (quickStrikeLevel > 0) {
                        float maxDamage = (float) ((LivingEntity) event.getSource().getEntity())
                                .getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                        float multiplier = quickStrikeLevel * 0.05f + 0.2f;

                        if (event.getAmount() <  multiplier * maxDamage) {
                            event.setAmount(multiplier * maxDamage);
                        }
                    }

                    int armorPenetrationLevel = getEffectLevel(itemStack, ItemEffect.armorPenetration);
                    if (armorPenetrationLevel > 0) {
                        ArmorPenetrationEffect.onLivingHurt(event, armorPenetrationLevel);
                    }
                });

        if (!event.getSource().isBypassArmor()) {
            Optional.ofNullable(event.getEntityLiving())
                    .map(entity -> Stream.of(entity.getMainHandItem(), entity.getOffhandItem()))
                    .orElseGet(Stream::empty)
                    .filter(itemStack -> !itemStack.isEmpty())
                    .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                    .forEach(itemStack -> {
                        ItemModularHandheld item = (ItemModularHandheld) itemStack.getItem();
                        if (item.getAttributeValue(itemStack, Attributes.ARMOR) > 0 || item.getAttributeValue(itemStack, Attributes.ARMOR_TOUGHNESS) > 0) {
                            int reducedAmount = (int) Math.ceil(event.getAmount() - CombatRules.getDamageAfterAbsorb(event.getAmount(),
                                    (float) event.getEntityLiving().getArmorValue(),
                                    (float) event.getEntityLiving().getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()));
                            item.applyUsageEffects(event.getEntityLiving(), itemStack, reducedAmount);
                            item.applyDamage(reducedAmount, itemStack, event.getEntityLiving());
                        }
                    });
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        Optional.ofNullable(event.getSource().getEntity())
                .filter(entity -> entity instanceof PlayerEntity)
                .map(entity -> (LivingEntity) entity)
                .map(LivingEntity::getMainHandItem)
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .ifPresent(itemStack -> {
                    int crushingLevel = getEffectLevel(itemStack, ItemEffect.crushing);
                    if (crushingLevel > 0) {
                        CrushingEffect.onLivingDamage(event, crushingLevel);
                    }

                    int skeweringLevel = getEffectLevel(itemStack, ItemEffect.skewering);
                    if (skeweringLevel > 0) {
                        SkeweringEffect.onLivingDamage(event, skeweringLevel, itemStack);
                    }
                });

        ArmorPenetrationEffect.onLivingDamage(event);
    }


    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        Optional.ofNullable(event.getEntityLiving().getEffect(EarthboundPotionEffect.instance))
                .ifPresent(effect -> event.getEntityLiving().setDeltaMovement(event.getEntityLiving().getDeltaMovement().multiply(1, 0.5, 1)));
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Optional.ofNullable(event.getEntityLiving())
                .map(LivingEntity::getMainHandItem)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .ifPresent(itemStack -> {
                    int backstabLevel = getEffectLevel(itemStack, ItemEffect.backstab);
                    if (backstabLevel > 0 && event.getTarget() instanceof LivingEntity) {
                        LivingEntity attacker = event.getEntityLiving();
                        LivingEntity target = (LivingEntity) event.getTarget();
                        if (180 - Math.abs(Math.abs(attacker.yHeadRot - target.yHeadRot) % 360 - 180) < 60) {
                            event.setDamageModifier(Math.max(1.25f + 0.25f * backstabLevel, event.getDamageModifier()));
                            event.setResult(Event.Result.ALLOW);
                        }
                    }

                    int critLevel = getEffectLevel(itemStack, ItemEffect.criticalStrike);
                    if (critLevel > 0) {
                        CritEffect.critEntity(event, itemStack, critLevel);
                    }
                });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClickInput(InputEvent.ClickInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack itemStack = mc.player.getMainHandItem();
        if (event.isAttack()
                && !event.isCanceled()
                && itemStack.getItem() instanceof ItemModularHandheld
                && mc.hitResult != null
                && RayTraceResult.Type.MISS.equals(mc.hitResult.getType())) {
            if (getEffectLevel(itemStack, ItemEffect.truesweep) > 0) {
                SweepingEffect.triggerTruesweep();
            }
            if (getEffectLevel(itemStack, ItemEffect.howling) > 0) {
                HowlingEffect.sendPacket();
            }
        }

        if (event.isUseItem()) {
            LungeEffect.onRightClick(mc.player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        KeyBinding jumpKey = Minecraft.getInstance().options.keyJump;
        if (jumpKey.matches(event.getKey(), event.getScanCode()) && jumpKey.isDown()) {
            LungeEffect.onJump(Minecraft.getInstance().player);
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
                        return;
                    }

                    if (!event.getWorld().isClientSide) {
                        int critLevel = getEffectLevel(itemStack, ItemEffect.criticalStrike);
                        if (critLevel > 0) {
                            if (CritEffect.critBlock(world, breakingPlayer, pos, blockState, itemStack, critLevel)) {
                                event.setCanceled(true);
                            }
                        }


                        if (breakingPlayer.getAttackStrengthScale(0.5f) > 0.9f) {
                            if (getEffectLevel(itemStack, ItemEffect.truesweep) > 0 && breakingPlayer.isOnGround() && !breakingPlayer.isSprinting()) {
                                SweepingEffect.truesweep(itemStack, breakingPlayer);
                            }

                            int howlingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.howling);
                            if (howlingLevel > 0) {
                                HowlingEffect.trigger(itemStack, breakingPlayer, howlingLevel);
                            }
                        }
                    }
                });
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ExhaustedPotionEffect.onBreakSpeed(event);
    }

    @SubscribeEvent
    public void onEnderTeleport(EnderTeleportEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide) {
            AxisAlignedBB aabb = new AxisAlignedBB(
                    event.getTargetX() - 24, event.getTargetY() - 24, event.getTargetZ() - 24,
                    event.getTargetX() + 24, event.getTargetY() + 24, event.getTargetZ() + 24);

            event.getEntity().getCommandSenderWorld().getEntitiesOfClass(PlayerEntity.class, aabb).forEach(player -> {
                int reverbLevel = PropertyHelper.getPlayerEffectLevel(player, ItemEffect.enderReverb);
                if (reverbLevel > 0 && !player.isCreative()) {
                    double effectProbability = PropertyHelper.getPlayerEffectEfficiency(player, ItemEffect.enderReverb);
                    if (effectProbability > 0) {
                        if (player.getRandom().nextDouble() < effectProbability * 2) {
                            player.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
                            player.addEffect(new EffectInstance(Effects.CONFUSION, 40 * reverbLevel));
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onArrowNock(ArrowNockEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!event.hasAmmo() && player.getItemInHand(Hand.OFF_HAND).isEmpty()) {
            ItemStack itemStack = ToolbeltHelper.findToolbelt(player);
            if (!itemStack.isEmpty()) {
                QuiverInventory inventory = new QuiverInventory(itemStack);
                List<Collection<ItemEffect>> effects = inventory.getSlotEffects();
                int count = CastOptional.cast(event.getBow().getItem(), IModularItem.class)
                        .map(item -> getEffectLevel(event.getBow(), ItemEffect.multishot))
                        .filter(level -> level > 0)
                        .orElse(1);

                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    if (effects.get(i).contains(ItemEffect.quickAccess) && !inventory.getItem(i).isEmpty()) {

                        player.setItemInHand(Hand.OFF_HAND, inventory.getItem(i).split(count));
                        player.startUsingItem(event.getHand());
                        inventory.setChanged();

                        event.setAction(new ActionResult<>(ActionResultType.SUCCESS, event.getBow()));
                        return;
                    }
                }
            }
        }
    }
}
