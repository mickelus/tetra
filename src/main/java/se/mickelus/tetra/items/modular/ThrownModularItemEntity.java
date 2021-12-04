package se.mickelus.tetra.items.modular;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.effect.CritEffect;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.ToolActionHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;

@ParametersAreNonnullByDefault
public class ThrownModularItemEntity extends AbstractArrow implements IEntityAdditionalSpawnData {
    public static final String unlocalizedName = "thrown_modular_item";
    public static final String stackKey = "stack";
    public static final String dealtDamageKey = "dealtDamage";
    private static final EntityDataAccessor<Byte> LOYALTY_LEVEL = SynchedEntityData.defineId(ThrownModularItemEntity.class, EntityDataSerializers.BYTE);
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ThrownModularItemEntity> type;
    public int returningTicks;
    private ItemStack thrownStack = new ItemStack(Items.TRIDENT);
    private boolean dealtDamage;
    private IntOpenHashSet hitEntities = new IntOpenHashSet(5);
    private int hitBlocks;

    public ThrownModularItemEntity(EntityType<? extends ThrownModularItemEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public ThrownModularItemEntity(Level worldIn, LivingEntity thrower, ItemStack thrownStackIn) {
        super(type, thrower, worldIn);
        thrownStack = thrownStackIn.copy();
        entityData.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyalty(thrownStackIn));


        CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class).ifPresent(item -> {
            double critModifier = CritEffect.rollMultiplier(thrower.getRandom(), item, thrownStack);
            setPierceLevel((byte) Math.round(getEffectLevel(ItemEffect.piercing) * critModifier));

            if (critModifier != 1d && level instanceof ServerLevel) {
                Vec3 pos = thrower.getEyePosition(0).add(thrower.getLookAngle());
                ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANTED_HIT,
                        pos.x(), pos.y(), pos.z(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        });

        if (thrownStack.getItem() instanceof ModularSingleHeadedItem) {
            setSoundEvent(SoundEvents.TRIDENT_HIT_GROUND);
        } else if (thrownStack.getItem() instanceof ModularShieldItem) {
            setSoundEvent(SoundEvents.PLAYER_ATTACK_KNOCKBACK);
        } else {
            setSoundEvent(SoundEvents.PLAYER_ATTACK_WEAK);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public ThrownModularItemEntity(Level worldIn, double x, double y, double z) {
        super(type, x, y, z, worldIn);
    }

    public ThrownModularItemEntity(PlayMessages.SpawnEntity packet, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LOYALTY_LEVEL, (byte) 0);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        if (inGroundTime > 4) {
            dealtDamage = true;
        }

        Entity shooter = getOwner();
        if ((dealtDamage || isNoPhysics()) && shooter != null) {
            int loyaltyLevel = entityData.get(LOYALTY_LEVEL);
            if (loyaltyLevel > 0 && !shouldReturnToThrower()) {
                if (!level.isClientSide && pickup == AbstractArrow.Pickup.ALLOWED) {
                    spawnAtLocation(getPickupItem(), 0.1f);
                }

                remove();
            } else if (loyaltyLevel > 0) {
                setNoPhysics(true);
                Vec3 Vector3d = new Vec3(shooter.getX() - getX(), shooter.getEyeY() - getY(), shooter.getZ() - getZ());
                setPosRaw(getX(), getY() + Vector3d.y * 0.015 * (double) loyaltyLevel, getZ());
                if (level.isClientSide) {
                    yOld = getY();
                }

                double speed = 0.05 * loyaltyLevel;
                setDeltaMovement(getDeltaMovement().scale(0.95).add(Vector3d.normalize().scale(speed)));
                if (returningTicks == 0) {
                    playSound(SoundEvents.TRIDENT_RETURN, 10f, 1f);
                    setDeltaMovement(Vector3d.scale(0.01));
                }

                ++returningTicks;
            }
        }

        super.tick();
    }

    private boolean shouldReturnToThrower() {
        Entity entity = getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        } else {
            return false;
        }
    }

    public boolean hasDealtDamage() {
        return dealtDamage;
    }

    public boolean isOnGround() {
        return inGroundTime > 0;
    }

    private int getEffectLevel(ItemEffect effect) {
        return CastOptional.cast(thrownStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectLevel(thrownStack, effect))
                .orElse(-1);
    }

    @Override
    protected ItemStack getPickupItem() {
        return thrownStack.copy();
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (rayTraceResult.getType() == HitResult.Type.BLOCK && !dealtDamage) {
            BlockPos pos = ((BlockHitResult) rayTraceResult).getBlockPos();
            Entity shooter = getOwner();
            BlockState blockState = level.getBlockState(pos);

            ItemModularHandheld item = CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class).orElse(null);
            if (ToolActionHelper.isEffectiveOn(thrownStack, blockState)	&& shooter instanceof Player player && item != null) {
                double destroySpeed = item.getDestroySpeed(thrownStack, blockState) * item.getEffectEfficiency(thrownStack, ItemEffect.throwable);

                if (destroySpeed > blockState.getDestroySpeed(level, pos)) {
                    if (shooter instanceof ServerPlayer serverPlayer) {
                        EffectHelper.sendEventToPlayer(serverPlayer, 2001, pos, Block.getId(blockState));
                    }

                    item.applyBreakEffects(thrownStack, level, blockState, pos, player);

                    hitBlocks++;
                    boolean canPierce = getEffectLevel(ItemEffect.piercingHarvest) > 0 && hitBlocks < getPierceLevel();
                    if (canPierce) {
                        setDeltaMovement(getDeltaMovement().normalize().scale(0.8f));
                    } else {
                        dealtDamage = true;
                        super.onHit(rayTraceResult);
                    }

                    breakBlock(player, pos, blockState);

                    if (canPierce) {
                        hitAdditional();
                    }

                    return;
                }
            }
        }

        super.onHit(rayTraceResult);
    }

    /**
     * A thrown tool that travel faster than 1 block per tick and can break several blocks, needs to break several blocks per tick or it will stop.
     * This is called recursively
     */
    private void hitAdditional() {
        Vec3 position = position();
        Vec3 target = position.add(getDeltaMovement());
        HitResult rayTraceResult = level.clip(new ClipContext(position, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (rayTraceResult.getType() == HitResult.Type.BLOCK
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, rayTraceResult)) {
            onHit(rayTraceResult);
        }
    }

    /**
     * Hacky way to break blocks in such a way that it seems as if the player is holding the thrown item
     */
    private void breakBlock(Player shooter, BlockPos pos, BlockState blockState) {
        ItemStack currentItem = shooter.getMainHandItem();

        shooter.setItemInHand(InteractionHand.MAIN_HAND, thrownStack);
        EffectHelper.breakBlock(level, shooter, thrownStack, pos, blockState, true);
        shooter.setItemInHand(InteractionHand.MAIN_HAND, currentItem);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult raytrace) {
        Entity target = raytrace.getEntity();
        Entity shooter = getOwner();
        Player playerShooter = CastOptional.cast(shooter, Player.class).orElse(null);

        DamageSource damagesource = DamageSource.trident(this, (shooter == null ? this : shooter));
        SoundEvent soundevent = SoundEvents.TRIDENT_HIT;

        int pierceLevel = getEffectLevel(ItemEffect.piercing);
        int ricochetLevel = getEffectLevel(ItemEffect.ricochet);

        if (pierceLevel > 0 || ricochetLevel > 0) {
            if (hitEntities == null) {
                hitEntities = new IntOpenHashSet(5);
            }

            if (hitEntities.contains(target.getId())) {
                return;
            }

            if (hitEntities.size() < pierceLevel || hitEntities.size() < ricochetLevel) {
                hitEntities.add(target.getId());
            } else {
                dealtDamage = true;
            }
        } else {
            dealtDamage = true;
        }

        ItemStack heldTemp = null;
        if (playerShooter != null) {
            heldTemp = playerShooter.getMainHandItem();
            playerShooter.setItemInHand(InteractionHand.MAIN_HAND, thrownStack);
        }

        if (target instanceof LivingEntity && thrownStack.getItem() instanceof ItemModularHandheld) {
            LivingEntity targetLivingEntity = (LivingEntity) target;
            ItemModularHandheld item = (ItemModularHandheld) thrownStack.getItem();

            double critModifier = CritEffect.rollMultiplier(targetLivingEntity.getRandom(), item, thrownStack);
            double damage = item.getAbilityBaseDamage(thrownStack) * item.getEffectEfficiency(thrownStack, ItemEffect.throwable);

            damage += EnchantmentHelper.getDamageBonus(thrownStack, targetLivingEntity.getMobType());
            damage *= critModifier;

            if (target.hurt(damagesource, (float) damage)) {
                if (shooter instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(targetLivingEntity, shooter);
                    EffectHelper.applyEnchantmentHitEffects(getPickupItem(), targetLivingEntity, (LivingEntity) shooter);
                    ItemEffectHandler.applyHitEffects(thrownStack, targetLivingEntity, (LivingEntity) shooter);

                    item.tickProgression((LivingEntity) shooter, thrownStack, 1);
                }

                doPostHurtEffects(targetLivingEntity);

                if (critModifier != 1d && !level.isClientSide) {
                    Vec3 hitVec = raytrace.getLocation();
                    ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANTED_HIT,
                            hitVec.x(), hitVec.y(), hitVec.z(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
                }
            }
        }

        float f1 = 1.0F;
        if (!level.isClientSide && level.isThundering() && EnchantmentHelper.hasChanneling(thrownStack)) {
            BlockPos blockpos = target.blockPosition();
            if (level.canSeeSky(blockpos)) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level);
                lightning.moveTo(Vec3.atBottomCenterOf(blockpos));
                lightning.setCause(shooter instanceof ServerPlayer ? (ServerPlayer) shooter : null);
                this.level.addFreshEntity(lightning);
                soundevent = SoundEvents.TRIDENT_THUNDER;
                f1 = 5.0F;
            }
        }

        if (playerShooter != null) {
            playerShooter.setItemInHand(InteractionHand.MAIN_HAND, heldTemp);
        }

        playSound(soundevent, f1, 1.0F);

        if (dealtDamage) {
            setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        } else if (ricochetLevel > 0 && !level.isClientSide) {
            Vec3 hitPos = raytrace.getLocation();
            setPosRaw(hitPos.x(), hitPos.y(), hitPos.z());
            setDeltaMovement(level.getEntities(shooter, new AABB(target.blockPosition()).inflate(8d), entity ->
                            !hitEntities.contains(entity.getId())
                                    && entity instanceof LivingEntity
                                    && !entity.isInvulnerableTo(damagesource)
                                    && (shooter == null || !entity.isAlliedTo(shooter)))
                    .stream()
                    .map(entity -> entity.position().add(0, entity.getBbHeight() * 0.8, 0))
                    .map(pos -> pos.subtract(position()))
                    .min(Comparator.comparing(Vec3::lengthSqr))
                    .map(Vec3::normalize)
                    .map(direction -> direction.multiply(1, 0.5, 1))
                    .map(direction -> direction.scale(Math.max(getDeltaMovement().length() * 0.5, 0.3)))
                    .orElse(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D)));
        }
    }

    /**
     * Checks if this projectile can hit the provided target
     *
     * @param target
     * @return
     */
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && (this.hitEntities == null || !this.hitEntities.contains(target.getId()));
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void playerTouch(Player entityIn) {
        Entity entity = getOwner();
        if (entity == null || entity.getUUID() == entityIn.getUUID()) {
            super.playerTouch(entityIn);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(stackKey, 10)) {
            thrownStack = ItemStack.of(compound.getCompound(stackKey));
        }

        dealtDamage = compound.getBoolean(dealtDamageKey);

        entityData.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyalty(thrownStack));

        if (thrownStack.getItem() instanceof ModularSingleHeadedItem) {
            setSoundEvent(SoundEvents.TRIDENT_HIT_GROUND);
        } else if (thrownStack.getItem() instanceof ModularShieldItem) {
            setSoundEvent(SoundEvents.PLAYER_ATTACK_KNOCKBACK);
        } else {
            setSoundEvent(SoundEvents.PLAYER_ATTACK_WEAK);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put(stackKey, thrownStack.save(new CompoundTag()));
        compound.putBoolean(dealtDamageKey, dealtDamage);
    }

    public void tickDespawn() {
        int level = this.entityData.get(LOYALTY_LEVEL);
        if (this.pickup != Pickup.ALLOWED || level <= 0) {
            super.tickDespawn();
        }

    }

    protected float getWaterInertia() {
        return 0.99F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeItem(thrownStack);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        thrownStack = buffer.readItem();
    }
}
