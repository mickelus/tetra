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

public class ThrownModularItemEntity extends AbstractArrowEntity implements IEntityAdditionalSpawnData {
    public static final String unlocalizedName = "thrown_modular_item";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ThrownModularItemEntity> type;

    private ItemStack thrownStack = new ItemStack(Items.TRIDENT);

    public static final String stackKey = "stack";
    public static final String dealtDamageKey = "dealtDamage";
    private static final DataParameter<Byte> LOYALTY_LEVEL = EntityDataManager.createKey(ThrownModularItemEntity.class, DataSerializers.BYTE);

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
        dataManager.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyaltyModifier(thrownStackIn));


        CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class).ifPresent(item -> {
            double critModifier = CritEffect.rollMultiplier(thrower.getRNG(), item, thrownStack);
            setPierceLevel((byte) Math.round(getEffectLevel(ItemEffect.piercing) * critModifier));

            if (critModifier != 1d && world instanceof ServerWorld) {
                Vector3d pos = thrower.getEyePosition(0).add(thrower.getLookVec());
                ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANTED_HIT,
                        pos.getX(), pos.getY(), pos.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        });

        if (thrownStack.getItem() instanceof ModularSingleHeadedItem) {
            setHitSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND);
        } else if (thrownStack.getItem() instanceof ModularShieldItem) {
            setHitSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK);
        } else {
            setHitSound(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK);
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
    protected void registerData() {
        super.registerData();
        dataManager.register(LOYALTY_LEVEL, (byte)0);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        if (timeInGround > 4) {
            dealtDamage = true;
        }

        Entity shooter = func_234616_v_();
        if ((dealtDamage || getNoClip()) && shooter != null) {
            int loyaltyLevel = dataManager.get(LOYALTY_LEVEL);
            if (loyaltyLevel > 0 && !shouldReturnToThrower()) {
                if (!world.isRemote && pickupStatus == AbstractArrowEntity.PickupStatus.ALLOWED) {
                    entityDropItem(getArrowStack(), 0.1f);
                }

                remove();
            } else if (loyaltyLevel > 0) {
                setNoClip(true);
                Vector3d Vector3d = new Vector3d(shooter.getPosX() - getPosX(), shooter.getPosYEye() - getPosY(), shooter.getPosZ() - getPosZ());
                setRawPosition(getPosX(), getPosY() + Vector3d.y * 0.015 * (double)loyaltyLevel, getPosZ());
                if (world.isRemote) {
                    lastTickPosY = getPosY();
                }

                double speed = 0.05 * loyaltyLevel;
                setMotion(getMotion().scale(0.95).add(Vector3d.normalize().scale(speed)));
                if (returningTicks == 0) {
                    playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10f, 1f);
                    setMotion(Vector3d.scale(0.01));
                }

                ++returningTicks;
            }
        }

        super.tick();
    }

    private boolean shouldReturnToThrower() {
        Entity entity = func_234616_v_();
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
        return timeInGround > 0;
    }

    private int getEffectLevel(ItemEffect effect) {
        return CastOptional.cast(thrownStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectLevel(thrownStack, effect))
                .orElse(-1);
    }

    @Override
    protected ItemStack getArrowStack() {
        return thrownStack.copy();
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    @Override
    protected EntityRayTraceResult rayTraceEntities(Vector3d startVec, Vector3d endVec) {
        return dealtDamage ? null : super.rayTraceEntities(startVec, endVec);
    }

    @Override
    protected void onImpact(RayTraceResult rayTraceResult) {
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK && !dealtDamage) {
            BlockPos pos = ((BlockRayTraceResult) rayTraceResult).getPos();
            Entity shooter = func_234616_v_();
            BlockState blockState = world.getBlockState(pos);

            ItemModularHandheld item = CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class).orElse(null);
            if (ForgeHooks.isToolEffective(world, pos, thrownStack)
                    && shooter instanceof PlayerEntity
                    && item != null) {
                double destroySpeed = item.getDestroySpeed(thrownStack, blockState);

                if (destroySpeed > 1
                        && destroySpeed  * item.getEffectEfficiency(thrownStack, ItemEffect.throwable) > blockState.getBlockHardness(world, pos)) {
                    if (shooter instanceof ServerPlayerEntity) {
                        EffectHelper.sendEventToPlayer((ServerPlayerEntity) shooter, 2001, pos, Block.getStateId(blockState));
                    }

                    item.applyBreakEffects(thrownStack, world, blockState, pos, (PlayerEntity) shooter);

                    hitBlocks++;
                    boolean canPierce = getEffectLevel(ItemEffect.piercingHarvest) > 0 && hitBlocks < getPierceLevel();
                    if (canPierce) {
                        setMotion(getMotion().normalize().scale(0.8f));
                    } else {
                        dealtDamage = true;
                        super.onImpact(rayTraceResult);
                    }

                    breakBlock((PlayerEntity) shooter, pos, blockState);

                    if (canPierce) {
                        hitAdditional();
                    }

                    return;
                }
            }
        }

        super.onImpact(rayTraceResult);
    }

    /**
     * A thrown tool that travel faster than 1 block per tick and can break several blocks, needs to break several blocks per tick or it will stop.
     * This is called recursively
     */
    private void hitAdditional() {
        Vector3d position = getPositionVec();
        Vector3d target = position.add(getMotion());
        RayTraceResult rayTraceResult = world.rayTraceBlocks(
                new RayTraceContext(position, target, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));

        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, rayTraceResult)) {
            onImpact(rayTraceResult);
        }
    }

    /**
     * Hacky way to break blocks in such a way that it seems as if the player is holding the thrown item
     */
    private void breakBlock(PlayerEntity shooter, BlockPos pos, BlockState blockState) {
        ItemStack currentItem = shooter.getHeldItemMainhand();

        shooter.setHeldItem(Hand.MAIN_HAND, thrownStack);
        EffectHelper.breakBlock(world, shooter, thrownStack, pos, blockState, true);
        shooter.setHeldItem(Hand.MAIN_HAND, currentItem);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onEntityHit(EntityRayTraceResult raytrace) {
        Entity target = raytrace.getEntity();
        Entity shooter = func_234616_v_();
        PlayerEntity playerShooter = CastOptional.cast(shooter, PlayerEntity.class).orElse(null);

        DamageSource damagesource = DamageSource.causeTridentDamage(this, (shooter == null ? this : shooter));
        SoundEvent soundevent = SoundEvents.ITEM_TRIDENT_HIT;

        int pierceLevel = getEffectLevel(ItemEffect.piercing);
        int ricochetLevel = getEffectLevel(ItemEffect.ricochet);

        if (pierceLevel > 0 || ricochetLevel > 0) {
            if (hitEntities == null) {
                hitEntities = new IntOpenHashSet(5);
            }

            if (hitEntities.contains(target.getEntityId())) {
                return;
            }

            if (hitEntities.size() < pierceLevel || hitEntities.size() < ricochetLevel) {
                hitEntities.add(target.getEntityId());
            } else {
                dealtDamage = true;
            }
        } else {
            dealtDamage = true;
        }

        ItemStack heldTemp = null;
        if (playerShooter != null) {
             heldTemp = playerShooter.getHeldItemMainhand();
            playerShooter.setHeldItem(Hand.MAIN_HAND, thrownStack);
        }

        if (target instanceof LivingEntity && thrownStack.getItem() instanceof ItemModularHandheld) {
            LivingEntity targetLivingEntity = (LivingEntity) target;
            ItemModularHandheld item = (ItemModularHandheld) thrownStack.getItem();

            double critModifier = CritEffect.rollMultiplier(targetLivingEntity.getRNG(), item, thrownStack);
            double damage = item.getAbilityBaseDamage(thrownStack) * item.getEffectEfficiency(thrownStack, ItemEffect.throwable);

            damage += EnchantmentHelper.getModifierForCreature(thrownStack, targetLivingEntity.getCreatureAttribute());
            damage *= critModifier;

            if (target.attackEntityFrom(damagesource, (float) damage)) {
                if (shooter instanceof LivingEntity) {
                    EnchantmentHelper.applyThornEnchantments(targetLivingEntity, shooter);
                    EffectHelper.applyEnchantmentHitEffects(getArrowStack(), targetLivingEntity, (LivingEntity) shooter);
                    ItemEffectHandler.applyHitEffects(thrownStack, targetLivingEntity, (LivingEntity) shooter);

                    item.tickProgression((LivingEntity) shooter, thrownStack, 1);
                }

                arrowHit(targetLivingEntity);

                if (critModifier != 1d && !world.isRemote) {
                    Vector3d hitVec = raytrace.getHitVec();
                    ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANTED_HIT,
                            hitVec.getX(), hitVec.getY(), hitVec.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
                }
            }
        }

        float f1 = 1.0F;
        if (!world.isRemote && world.isThundering() && EnchantmentHelper.hasChanneling(thrownStack)) {
            BlockPos blockpos = target.getPosition();
            if (world.canSeeSky(blockpos)) {
                LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(this.world);
                lightning.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
                lightning.setCaster(shooter instanceof ServerPlayerEntity ? (ServerPlayerEntity) shooter : null);
                this.world.addEntity(lightning);
                soundevent = SoundEvents.ITEM_TRIDENT_THUNDER;
                f1 = 5.0F;
            }
        }

        if (playerShooter != null) {
            playerShooter.setHeldItem(Hand.MAIN_HAND, heldTemp);
        }

        playSound(soundevent, f1, 1.0F);

        if (dealtDamage) {
            setMotion(getMotion().mul(-0.01D, -0.1D, -0.01D));
        } else if (ricochetLevel > 0 && !world.isRemote) {
            Vector3d hitPos = raytrace.getHitVec();
            setRawPosition(hitPos.getX(), hitPos.getY(), hitPos.getZ());
            setMotion(world.getEntitiesInAABBexcluding(shooter, new AxisAlignedBB(target.getPosition()).grow(8d), entity ->
                    !hitEntities.contains(entity.getEntityId())
                            && entity instanceof LivingEntity
                            && !entity.isInvulnerableTo(damagesource)
                            && (shooter == null || !entity.isOnSameTeam(shooter)))
                    .stream()
                    .map(entity -> entity.getPositionVec().add(0, entity.getHeight() * 0.8, 0))
                    .map(pos -> pos.subtract(getPositionVec()))
                    .min(Comparator.comparing(Vector3d::lengthSquared))
                    .map(Vector3d::normalize)
                    .map(direction -> direction.mul(1, 0.5, 1))
                    .map(direction -> direction.scale(Math.max(getMotion().length() * 0.5, 0.3)))
                    .orElse(getMotion().mul(-0.01D, -0.1D, -0.01D)));
        }
    }

    /**
     * Checks if this projectile can hit the provided target
     * @param target
     * @return
     */
    protected boolean func_230298_a_(Entity target) {
        return super.func_230298_a_(target) && (this.hitEntities == null || !this.hitEntities.contains(target.getEntityId()));
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        Entity entity = func_234616_v_();
        if (entity == null || entity.getUniqueID() == entityIn.getUniqueID()) {
            super.onCollideWithPlayer(entityIn);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains(stackKey, 10)) {
            thrownStack = ItemStack.read(compound.getCompound(stackKey));
        }

        dealtDamage = compound.getBoolean(dealtDamageKey);

        dataManager.set(LOYALTY_LEVEL, (byte)EnchantmentHelper.getLoyaltyModifier(thrownStack));

        if (thrownStack.getItem() instanceof ModularSingleHeadedItem) {
            setHitSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND);
        } else if (thrownStack.getItem() instanceof ModularShieldItem) {
            setHitSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK);
        } else {
            setHitSound(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK);
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.put(stackKey, thrownStack.write(new CompoundNBT()));
        compound.putBoolean(dealtDamageKey, dealtDamage);
    }

    public void func_225516_i_() {
        int level = this.dataManager.get(LOYALTY_LEVEL);
        if (this.pickupStatus != PickupStatus.ALLOWED || level <= 0) {
            super.func_225516_i_();
        }

    }

    protected float getWaterDrag() {
        return 0.99F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeItemStack(thrownStack);
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        thrownStack = buffer.readItemStack();
    }
}
