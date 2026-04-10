package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;
import se.mickelus.tetra.compat.forge.registries.ObjectHolder;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.mutil.util.RotationHelper;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ExtractorProjectileEntity extends AbstractArrow implements IEntityWithComplexSpawn {
    public static final String unlocalizedName = "extractor_projectile";
    public static final String damageKey = "dmg";
    public static final String heatKey = "heat";
    @ObjectHolder(registryName = "entity_type", value = TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ExtractorProjectileEntity> type;
    private int damage;
    private int heat;

    private boolean extinguishing = false;

    public ExtractorProjectileEntity(Level world, LivingEntity shooter, ItemStack itemStack) {
        super(type, shooter, world, itemStack.copy(), null);

        damage = itemStack.getDamageValue();

        initDefaults();
    }

    public ExtractorProjectileEntity(EntityType<? extends ExtractorProjectileEntity> type, Level worldIn) {
        super(type, worldIn);
        initDefaults();
    }


    @OnlyIn(Dist.CLIENT)
    public ExtractorProjectileEntity(Level worldIn, double x, double y, double z) {
        super(type, x, y, z, worldIn, new ItemStack(ChthonicExtractorBlock.item), null);
        initDefaults();
    }

    private void initDefaults() {
        setSoundEvent(SoundEvents.NETHERITE_BLOCK_HIT);
        setBaseDamage(0.5);
        pickup = Pickup.ALLOWED;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (!level().isClientSide
                && rayTraceResult.getType() == HitResult.Type.BLOCK
                && getDeltaMovement().lengthSqr() > 0.95) {
            ServerPlayer shooter = CastOptional.cast(getOwner(), ServerPlayer.class).orElse(null);
            BlockPos pos = ((BlockHitResult) rayTraceResult).getBlockPos();

            if (shooter != null && breakBlock(level(), pos, shooter)) {
                breakAround(level(), pos, ((BlockHitResult) rayTraceResult).getDirection(), shooter);
                setDeltaMovement(getDeltaMovement().scale(0.95f));
                hitAdditional();
                return;
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
        HitResult rayTraceResult = level().clip(
                new ClipContext(position, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (rayTraceResult.getType() == HitResult.Type.BLOCK
                && !EventHooks.onProjectileImpact(this, rayTraceResult)) {
            onHit(rayTraceResult);
        }
    }

    private void breakAround(Level world, BlockPos pos, Direction face, ServerPlayer shooter) {
        Vec3i axis1 = RotationHelper.shiftAxis(face.getNormal());
        Vec3i axis2 = RotationHelper.shiftAxis(axis1);
        ServerScheduler.schedule(2, () -> {
            breakBlock(world, pos.offset(axis1), shooter);
            breakBlock(world, pos.subtract(axis1), shooter);
        });
        ServerScheduler.schedule(4, () -> {
            breakBlock(world, pos.offset(axis2), shooter);
            breakBlock(world, pos.subtract(axis2), shooter);
        });
        ServerScheduler.schedule(6, () -> {
            breakBlock(world, pos.offset(axis1).offset(axis2), shooter);
            breakBlock(world, pos.subtract(axis1).subtract(axis2), shooter);
        });
        ServerScheduler.schedule(8, () -> {
            breakBlock(world, pos.offset(axis1).subtract(axis2), shooter);
            breakBlock(world, pos.subtract(axis1).offset(axis2), shooter);
        });
    }

    private boolean breakBlock(Level world, BlockPos pos, ServerPlayer shooter) {
        GameType gameType = shooter.gameMode.getGameModeForPlayer();
        BlockState blockState = world.getBlockState(pos);

        BlockEntity tileEntity = world.getBlockEntity(pos);

        if (blockState.getDestroySpeed(world, pos) != -1
                && isAlive()
                && !shooter.blockActionRestricted(world, pos, gameType)
                && blockState.is(FracturedBedrockTile.extractorBreakable)
                && !net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(world, gameType, shooter, pos, blockState).isCanceled()) {

            BlockState destroyedState = blockState.getBlock().playerWillDestroy(world, pos, blockState, shooter);
            boolean removed = destroyedState.getBlock().onDestroyedByPlayer(destroyedState, world, pos, shooter, true, world.getFluidState(pos));
            if (!removed) {
                return false;
            }

            destroyedState.getBlock().playerDestroy(world, shooter, pos, destroyedState, tileEntity, ItemStack.EMPTY);
            destroyedState.getBlock().destroy(world, pos, destroyedState);
            world.levelEvent(null, 2001, pos, Block.getId(destroyedState));
            damage++;
            heat += 10;

            if (damage > ChthonicExtractorBlock.maxDamage) {
                destroyExtractor();
            }

            return true;
        }

        return false;
    }

    private void destroyExtractor() {
        discard();
        level().explode(getOwner(), getX(), getY(), getZ(), 4, true, Level.ExplosionInteraction.TNT);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (onGround() && heat > 0) {
                int cooldown = isInWater() ? 10 : 1;
                if (tickCount % 10 == 0) {
                    Vec3 pos = position().add(getLookAngle().scale(-Math.random()));
                    ((ServerLevel) level()).sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, cooldown, 0,
                            0.01, 0, 0.01D);

                    ((ServerLevel) level()).sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.1, pos.z, 1, 0,
                            0.01, 0, 0.01D);
                }

                if (cooldown > 1 && !extinguishing) {
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.2f, 0.9f);
                    ((ServerLevel) level()).sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, getX(), getY(), getZ(), 12, 0,
                            0.01, 0, 0.01D);

                    extinguishing = true;
                }

                heat -= cooldown;
            } else {
                if (tickCount % 40 == 0) {
                    Vec3 pos = position().add(getLookAngle().scale(-Math.random()));
                    ((ServerLevel) level()).sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 1, 0,
                            0.01, 0, 0.01D);
                }
            }
        }
    }

    @Override
    public boolean onGround() {
        return inGroundTime > 0;
    }

    @Override
    protected ItemStack getPickupItem() {
        if (damage == 0) {
            return new ItemStack(ChthonicExtractorBlock.item);
        }

        ItemStack itemStack = new ItemStack(ChthonicExtractorBlock.usedItem);
        itemStack.setDamageValue(damage);
        return itemStack;
    }

    @Override
    protected void onHitBlock(BlockHitResult rayTraceResult) {
        super.onHitBlock(rayTraceResult);
        this.setSoundEvent(SoundEvents.NETHERITE_BLOCK_HIT);
    }

    @Override
    protected void onHitEntity(EntityHitResult rayTraceResult) {
        super.onHitEntity(rayTraceResult);
        setDeltaMovement(getDeltaMovement().normalize().scale(-0.1));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !type.equals(entity.getType()) && super.canHitEntity(entity);
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void playerTouch(Player player) {
        if (inGround) {
            super.playerTouch(player);

            // this should mean that it has been picked up
            if (!isAlive()) {
                ignitePlayer(player);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    // pretty much the same as a regular pickup but attempts to place it in the offhand first
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (!level().isClientSide
                && onGround()
                && isAlive()
                && pickup == AbstractArrow.Pickup.ALLOWED) {

            ItemStack itemStack = getPickupItem();
            boolean success = false;

            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                success = true;
            } else if (player.getOffhandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND, itemStack);
                success = true;
            } else if (player.getInventory().add(itemStack)) {
                success = true;
            }

            if (success) {
                player.take(this, 1);
                ignitePlayer(player);
                discard();

                return InteractionResult.SUCCESS;
            }
        }
        return super.interactAt(player, vec, hand);
    }

    private void ignitePlayer(Player player) {
        if (!isAlive() && heat > 10) {
            player.igniteForSeconds(3 + heat / 20);
        }
    }

    // possibly stops this from being removed after sitting around for too long
    @Override
    public void tickDespawn() {
        if (this.pickup != Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        damage = compound.getInt(damageKey);
        heat = compound.getInt(heatKey);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt(damageKey, damage);
        compound.putInt(heatKey, heat);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ChthonicExtractorBlock.item);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(damage);
        buffer.writeInt(heat);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        damage = buffer.readInt();
        heat = buffer.readInt();
    }
}
