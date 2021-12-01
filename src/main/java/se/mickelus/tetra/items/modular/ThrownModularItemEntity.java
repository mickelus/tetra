package se.mickelus.tetra.items.modular;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.effect.CritEffect;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;

import net.minecraft.entity.projectile.AbstractArrowEntity.PickupStatus;

public class ThrownModularItemEntity extends AbstractArrowEntity implements IEntityAdditionalSpawnData {
    public static final String unlocalizedName = "thrown_modular_item";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ThrownModularItemEntity> type;

    private ItemStack thrownStack = new ItemStack(Items.TRIDENT);

    public static final String stackKey = "stack";
    public static final String dealtDamageKey = "dealtDamage";
    private static final DataParameter<Byte> LOYALTY_LEVEL = EntityDataManager.defineId(ThrownModularItemEntity.class, DataSerializers.BYTE);

    private boolean dealtDamage;
    public int returningTicks;

    private IntOpenHashSet hitEntities = new IntOpenHashSet(5);
    private int hitBlocks;

    public ThrownModularItemEntity(EntityType<? extends ThrownModularItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public ThrownModularItemEntity(World worldIn, LivingEntity thrower, ItemStack thrownStackIn) {
        super(type, thrower, worldIn);
        thrownStack = thrownStackIn.copy();
        entityData.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyalty(thrownStackIn));


        CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class).ifPresent(item -> {
            double critModifier = CritEffect.rollMultiplier(thrower.getRandom(), item, thrownStack);
            setPierceLevel((byte) Math.round(getEffectLevel(ItemEffect.piercing) * critModifier));

            if (critModifier != 1d && level instanceof ServerWorld) {
                Vector3d pos = thrower.getEyePosition(0).add(thrower.getLookAngle());
                ((ServerWorld) level).sendParticles(ParticleTypes.ENCHANTED_HIT,
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
    public ThrownModularItemEntity(World worldIn, double x, double y, double z) {
        super(type, x, y, z, worldIn);
    }

    public ThrownModularItemEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LOYALTY_LEVEL, (byte)0);
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
                if (!level.isClientSide && pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
                    spawnAtLocation(getPickupItem(), 0.1f);
                }

                remove();
            } else if (loyaltyLevel > 0) {
                setNoPhysics(true);
                Vector3d Vector3d = new Vector3d(shooter.getX() - getX(), shooter.getEyeY() - getY(), shooter.getZ() - getZ());
                setPosRaw(getX(), getY() + Vector3d.y * 0.015 * (double)loyaltyLevel, getZ());
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
            return !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
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
    protected EntityRayTraceResult findHitEntity(Vector3d startVec, Vector3d endVec) {
        return dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK && !dealtDamage) {
            BlockPos pos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
            Entity shooter = getOwner();
            BlockState blockState = level.getBlockState(pos);

            ItemModularHandheld item = CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class).orElse(null);
            if (ForgeHooks.isToolEffective(level, pos, thrownStack)
                    && shooter instanceof PlayerEntity
                    && item != null) {
                double destroySpeed = item.getDestroySpeed(thrownStack, blockState) * item.getEffectEfficiency(thrownStack, ItemEffect.throwable);

                if (destroySpeed > blockState.getDestroySpeed(level, pos)) {
                    if (shooter instanceof ServerPlayerEntity) {
                        EffectHelper.sendEventToPlayer((ServerPlayerEntity) shooter, 2001, pos, Block.getId(blockState));
                    }

                    item.applyBreakEffects(thrownStack, level, blockState, pos, (PlayerEntity) shooter);

                    hitBlocks++;
                    boolean canPierce = getEffectLevel(ItemEffect.piercingHarvest) > 0 && hitBlocks < getPierceLevel();
                    if (canPierce) {
                        setDeltaMovement(getDeltaMovement().normalize().scale(0.8f));
                    } else {
                        dealtDamage = true;
                        super.onHit(rayTraceResult);
                    }

                    breakBlock((PlayerEntity) shooter, pos, blockState);

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
        Vector3d position = position();
        Vector3d target = position.add(getDeltaMovement());
        RayTraceResult rayTraceResult = level.clip(
                new RayTraceContext(position, target, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));

        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, rayTraceResult)) {
            onHit(rayTraceResult);
        }
    }

    /**
     * Hacky way to break blocks in such a way that it seems as if the player is holding the thrown item
     */
    private void breakBlock(PlayerEntity shooter, BlockPos pos, BlockState blockState) {
        ItemStack currentItem = shooter.getMainHandItem();

        shooter.setItemInHand(Hand.MAIN_HAND, thrownStack);
        EffectHelper.breakBlock(level, shooter, thrownStack, pos, blockState, true);
        shooter.setItemInHand(Hand.MAIN_HAND, currentItem);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityRayTraceResult raytrace) {
        Entity target = raytrace.getEntity();
        Entity shooter = getOwner();
        PlayerEntity playerShooter = CastOptional.cast(shooter, PlayerEntity.class).orElse(null);

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
            playerShooter.setItemInHand(Hand.MAIN_HAND, thrownStack);
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
                    Vector3d hitVec = raytrace.getLocation();
                    ((ServerWorld) level).sendParticles(ParticleTypes.ENCHANTED_HIT,
                            hitVec.x(), hitVec.y(), hitVec.z(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
                }
            }
        }

        float f1 = 1.0F;
        if (!level.isClientSide && level.isThundering() && EnchantmentHelper.hasChanneling(thrownStack)) {
            BlockPos blockpos = target.blockPosition();
            if (level.canSeeSky(blockpos)) {
                LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(this.level);
                lightning.moveTo(Vector3d.atBottomCenterOf(blockpos));
                lightning.setCause(shooter instanceof ServerPlayerEntity ? (ServerPlayerEntity) shooter : null);
                this.level.addFreshEntity(lightning);
                soundevent = SoundEvents.TRIDENT_THUNDER;
                f1 = 5.0F;
            }
        }

        if (playerShooter != null) {
            playerShooter.setItemInHand(Hand.MAIN_HAND, heldTemp);
        }

        playSound(soundevent, f1, 1.0F);

        if (dealtDamage) {
            setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        } else if (ricochetLevel > 0 && !level.isClientSide) {
            Vector3d hitPos = raytrace.getLocation();
            setPosRaw(hitPos.x(), hitPos.y(), hitPos.z());
            setDeltaMovement(level.getEntities(shooter, new AxisAlignedBB(target.blockPosition()).inflate(8d), entity ->
                    !hitEntities.contains(entity.getId())
                            && entity instanceof LivingEntity
                            && !entity.isInvulnerableTo(damagesource)
                            && (shooter == null || !entity.isAlliedTo(shooter)))
                    .stream()
                    .map(entity -> entity.position().add(0, entity.getBbHeight() * 0.8, 0))
                    .map(pos -> pos.subtract(position()))
                    .min(Comparator.comparing(Vector3d::lengthSqr))
                    .map(Vector3d::normalize)
                    .map(direction -> direction.multiply(1, 0.5, 1))
                    .map(direction -> direction.scale(Math.max(getDeltaMovement().length() * 0.5, 0.3)))
                    .orElse(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D)));
        }
    }

    /**
     * Checks if this projectile can hit the provided target
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
    public void playerTouch(PlayerEntity entityIn) {
        Entity entity = getOwner();
        if (entity == null || entity.getUUID() == entityIn.getUUID()) {
            super.playerTouch(entityIn);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(stackKey, 10)) {
            thrownStack = ItemStack.of(compound.getCompound(stackKey));
        }

        dealtDamage = compound.getBoolean(dealtDamageKey);

        entityData.set(LOYALTY_LEVEL, (byte)EnchantmentHelper.getLoyalty(thrownStack));

        if (thrownStack.getItem() instanceof ModularSingleHeadedItem) {
            setSoundEvent(SoundEvents.TRIDENT_HIT_GROUND);
        } else if (thrownStack.getItem() instanceof ModularShieldItem) {
            setSoundEvent(SoundEvents.PLAYER_ATTACK_KNOCKBACK);
        } else {
            setSoundEvent(SoundEvents.PLAYER_ATTACK_WEAK);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.put(stackKey, thrownStack.save(new CompoundNBT()));
        compound.putBoolean(dealtDamageKey, dealtDamage);
    }

    public void tickDespawn() {
        int level = this.entityData.get(LOYALTY_LEVEL);
        if (this.pickup != PickupStatus.ALLOWED || level <= 0) {
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
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeItem(thrownStack);
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        thrownStack = buffer.readItem();
    }
}
