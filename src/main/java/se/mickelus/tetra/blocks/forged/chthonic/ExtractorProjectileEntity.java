package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.math.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.RotationHelper;

public class ExtractorProjectileEntity extends AbstractArrow implements IEntityAdditionalSpawnData {
    public static final String unlocalizedName = "extractor_projectile";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ExtractorProjectileEntity> type;

    public static final String damageKey = "dmg";
    private int damage;

    public static final String heatKey = "heat";
    private int heat;

    private boolean extinguishing = false;

    public ExtractorProjectileEntity(Level world, LivingEntity shooter, ItemStack itemStack) {
        super(type, shooter, world);

        damage = itemStack.getDamageValue();

        setSoundEvent(SoundEvents.NETHERITE_BLOCK_HIT);
        setBaseDamage(0.5);
        setKnockback(3);
        setPierceLevel(Byte.MAX_VALUE);
    }

    public ExtractorProjectileEntity(EntityType<? extends ExtractorProjectileEntity> type, Level worldIn) {
        super(type, worldIn);
        setBaseDamage(0.5);
        setKnockback(3);
        setPierceLevel(Byte.MAX_VALUE);
    }


    @OnlyIn(Dist.CLIENT)
    public ExtractorProjectileEntity(Level worldIn, double x, double y, double z) {
        super(type, x, y, z, worldIn);
        setBaseDamage(0.5);
        setKnockback(3);
        setPierceLevel(Byte.MAX_VALUE);
    }

    public ExtractorProjectileEntity(FMLPlayMessages.SpawnEntity packet, Level worldIn) {
        super(type, worldIn);
        setBaseDamage(0.5);
        setKnockback(3);
        setPierceLevel(Byte.MAX_VALUE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (!level.isClientSide
                && rayTraceResult.getType() == HitResult.Type.BLOCK
                && getDeltaMovement().lengthSqr() > 0.95) {
            ServerPlayer shooter = CastOptional.cast(getOwner(), ServerPlayer.class).orElse(null);
            BlockPos pos = ((BlockHitResult) rayTraceResult).getBlockPos();

            if (shooter != null && breakBlock(level, pos, shooter)) {
                breakAround(level, pos, ((BlockHitResult) rayTraceResult).getDirection(), shooter);
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
        HitResult rayTraceResult = level.clip(
                new ClipContext(position, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (rayTraceResult.getType() == HitResult.Type.BLOCK
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, rayTraceResult)) {
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
        ServerLevel serverWorld = (ServerLevel) world;
        GameType gameType = shooter.gameMode.getGameModeForPlayer();
        BlockState blockState = world.getBlockState(pos);

        BlockEntity tileEntity = world.getBlockEntity(pos);

        if (blockState.getDestroySpeed(world, pos) != -1
                && isAlive()
                && !shooter.blockActionRestricted(world, pos, gameType)
                && FracturedBedrockTile.breakMaterials.contains(blockState.getMaterial())
                && blockState.getBlock().removedByPlayer(blockState, world, pos, shooter, true, world.getFluidState(pos))
                && ForgeHooks.onBlockBreakEvent(world, gameType, shooter, pos) != -1) {

            blockState.getBlock().playerDestroy(world, shooter, pos, blockState, tileEntity, ItemStack.EMPTY);
            blockState.getBlock().destroy(world, pos, blockState);
            world.levelEvent(null, 2001, pos, Block.getId(blockState));
            damage++;
            heat += 10;

            // custom exp drop check since player is not holding an item that can harvest the block
            int exp = blockState.getExpDrop(world, pos, 0, 0);
            if (exp > 0) {
                blockState.getBlock().popExperience(serverWorld, pos, exp);
            }


            if (damage > ChthonicExtractorBlock.maxDamage) {
                destroyExtractor();
            }

            return true;
        }

        return false;
    }

    private void destroyExtractor() {
        remove();
        level.explode(getOwner(), getX(), getY(), getZ(), 4, Explosion.BlockInteraction.BREAK);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            if (isOnGround() && heat > 0) {
                int cooldown = isInWater() ? 10 : 1;
                if (tickCount % 10 == 0) {
                    Vec3 pos = position().add(getLookAngle().scale(-Math.random()));
                    ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, cooldown, 0,
                            0.01, 0, 0.01D);

                    ((ServerLevel) level).sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.1, pos.z, 1, 0,
                            0.01, 0, 0.01D);
                }

                if (cooldown > 1 && !extinguishing) {
                    level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.2f, 0.9f);
                    ((ServerLevel) level).sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, getX(), getY(), getZ(), 12, 0,
                            0.01, 0, 0.01D);

                    extinguishing = true;
                }

                heat -= cooldown;
            } else {
                if (tickCount % 40 == 0) {
                    Vec3 pos = position().add(getLookAngle().scale(-Math.random()));
                    ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 1, 0,
                            0.01, 0, 0.01D);
                }
            }
        }
    }

    @Override
    public boolean isOnGround() {
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
    protected void onHitEntity(EntityHitResult p_213868_1_) {
        super.onHitEntity(p_213868_1_);
        setDeltaMovement(getDeltaMovement().normalize().scale(-0.1));
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
        if (!level.isClientSide
                && isOnGround()
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
            } else if (player.inventory.add(itemStack)) {
                success = true;
            }

            if (success) {
                player.take(this, 1);
                ignitePlayer(player);
                remove();

                return InteractionResult.SUCCESS;
            }
        }
        return super.interactAt(player, vec, hand);
    }

    private void ignitePlayer(Player player) {
        if (!isAlive() && heat > 10) {
            player.setSecondsOnFire(3 + heat / 20);
        }
    }

    // possibly stops this from being removed after sitting around for too long
    @Override
    public void tickDespawn() { }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(damage);
        buffer.writeInt(heat);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        damage = buffer.readInt();
        heat = buffer.readInt();
    }
}
