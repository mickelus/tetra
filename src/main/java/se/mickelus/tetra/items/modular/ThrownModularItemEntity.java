package se.mickelus.tetra.items.modular;

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
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;

public class ThrownModularItemEntity extends AbstractArrowEntity implements IEntityAdditionalSpawnData {
    public static final String unlocalizedName = "thrown_modular_item";

    public static final String stackKey = "stack";
    public static final String dealtDamageKey = "DealtDamage";
    private static final DataParameter<Byte> LOYALTY_LEVEL = EntityDataManager.createKey(ThrownModularItemEntity.class, DataSerializers.BYTE);

    private ItemStack thrownStack = new ItemStack(Items.TRIDENT);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ThrownModularItemEntity> type;

    private boolean dealtDamage;
    public int returningTicks;

    public ThrownModularItemEntity(EntityType<? extends ThrownModularItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public ThrownModularItemEntity(World worldIn, LivingEntity thrower, ItemStack thrownStackIn) {
        super(type, thrower, worldIn);
        thrownStack = thrownStackIn.copy();
        dataManager.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyaltyModifier(thrownStackIn));
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

        Entity shooter = getShooter();
        if ((dealtDamage || getNoClip()) && shooter != null) {
            int loyaltyLevel = dataManager.get(LOYALTY_LEVEL);
            if (loyaltyLevel > 0 && !shouldReturnToThrower()) {
                if (!world.isRemote && pickupStatus == AbstractArrowEntity.PickupStatus.ALLOWED) {
                    entityDropItem(getArrowStack(), 0.1F);
                }

                remove();
            } else if (loyaltyLevel > 0) {
                setNoClip(true);
                Vec3d vec3d = new Vec3d(shooter.getPosX() - getPosX(), shooter.getPosYEye() - getPosY(), shooter.getPosZ() - getPosZ());
                setRawPosition(getPosX(), getPosY() + vec3d.y * 0.015D * (double)loyaltyLevel, getPosZ());
                if (world.isRemote) {
                    lastTickPosY = getPosY();
                }

                double speed = 0.05D * (double)loyaltyLevel;
                setMotion(getMotion().scale(0.95D).add(vec3d.normalize().scale(speed)));
                if (returningTicks == 0) {
                    playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                    setMotion(vec3d.scale(0.01));
                }

                ++returningTicks;
            }
        }

        super.tick();
    }

    private boolean shouldReturnToThrower() {
        Entity entity = getShooter();
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

    @Override
    protected ItemStack getArrowStack() {
        return thrownStack.copy();
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    @Override
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return dealtDamage ? null : super.rayTraceEntities(startVec, endVec);
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        super.onHit(rayTraceResult);

        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK && !dealtDamage) {
            BlockPos pos = ((BlockRayTraceResult)rayTraceResult).getPos();
            Entity shooter = getShooter();
            BlockState blockState = world.getBlockState(pos);

            if (ForgeHooks.canToolHarvestBlock(world, pos, thrownStack) && shooter instanceof PlayerEntity) {
                float destroySpeed = CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class)
                        .map(item -> item.getDestroySpeed(thrownStack, blockState))
                        .orElse(1f);

                if (destroySpeed > blockState.getBlockHardness(world, pos)) {
                    if (shooter instanceof ServerPlayerEntity) {
                        ItemEffectHandler.sendEventToPlayer((ServerPlayerEntity) shooter, 2001, pos, Block.getStateId(blockState));
                    }

                    CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class)
                            .ifPresent(item -> item.applyBreakEffects(thrownStack, world, blockState, pos, (PlayerEntity) shooter));

                    breakBlock((PlayerEntity) shooter, pos, blockState);

                    Vec3i faceVec = ((BlockRayTraceResult) rayTraceResult).getFace().getDirectionVec();
                    setMotion(new Vec3d(faceVec).scale(0.1));

                    dealtDamage = true;
                }
            }
        }
    }

    /**
     * Hacky way to break blocks in such a way that it seems as if the player is holding the thrown item
     */
    private void breakBlock(PlayerEntity shooter, BlockPos pos, BlockState blockState) {
        ItemStack currentItem = shooter.getHeldItemMainhand();

        shooter.setHeldItem(Hand.MAIN_HAND, thrownStack);
        ItemEffectHandler.breakBlock(world, shooter, thrownStack, pos, blockState, true);
        shooter.setHeldItem(Hand.MAIN_HAND, currentItem);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onEntityHit(EntityRayTraceResult raytrace) {
        Entity target = raytrace.getEntity();
        Entity shooter = getShooter();
        DamageSource damagesource = DamageSource.causeTridentDamage(this, (shooter == null ? this : shooter));
        dealtDamage = true;
        SoundEvent soundevent = SoundEvents.ITEM_TRIDENT_HIT;
        double damage = CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getAbilityBaseDamage(thrownStack))
                .orElse(8d);

        if (target instanceof LivingEntity) {
            LivingEntity targetLivingEntity = (LivingEntity)target;
            damage += EnchantmentHelper.getModifierForCreature(thrownStack, targetLivingEntity.getCreatureAttribute());

            if (target.attackEntityFrom(damagesource, (float) damage)) {
                if (shooter instanceof LivingEntity) {
                    EnchantmentHelper.applyThornEnchantments(targetLivingEntity, shooter);
                    ItemModularHandheld.applyEnchantmentHitEffects(getArrowStack(), targetLivingEntity, (LivingEntity) shooter);
                }

                arrowHit(targetLivingEntity);
            }
        }

        setMotion(getMotion().mul(-0.01D, -0.1D, -0.01D));
        float f1 = 1.0F;
        if (world instanceof ServerWorld && world.isThundering() && EnchantmentHelper.hasChanneling(thrownStack)) {
            BlockPos blockpos = target.getPosition();
            if (world.canSeeSky(blockpos)) {
                LightningBoltEntity lightningboltentity = new LightningBoltEntity(world, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, false);
                lightningboltentity.setCaster(shooter instanceof ServerPlayerEntity ? (ServerPlayerEntity)shooter : null);
                ((ServerWorld)world).addLightningBolt(lightningboltentity);
                soundevent = SoundEvents.ITEM_TRIDENT_THUNDER;
                f1 = 5.0F;
            }
        }


        if (target instanceof LivingEntity && shooter instanceof LivingEntity) {
            CastOptional.cast(thrownStack.getItem(), ItemModularHandheld.class)
                    .ifPresent(item -> item.applyHitEffects(thrownStack, (LivingEntity) target, (LivingEntity) shooter));
        }

        playSound(soundevent, f1, 1.0F);
    }

    /**
     * The sound made when an entity is hit by this projectile
     */
    @Override
    protected SoundEvent getHitEntitySound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        Entity entity = getShooter();
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
