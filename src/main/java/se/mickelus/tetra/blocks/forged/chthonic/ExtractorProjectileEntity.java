package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

public class ExtractorProjectileEntity extends AbstractArrowEntity implements IEntityAdditionalSpawnData {
    public static final String unlocalizedName = "extractor_projectile";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EntityType<ExtractorProjectileEntity> type;

    public static final String damageKey = "dmg";
    private int damage;

    public static final String heatKey = "heat";
    private int heat;

    private boolean extinguishing = false;

    public ExtractorProjectileEntity(World world, LivingEntity shooter, ItemStack itemStack) {
        super(type, shooter, world);

        damage = itemStack.getDamage();

        setHitSound(SoundEvents.BLOCK_NETHERITE_BLOCK_HIT);
        setDamage(0.5);
        setKnockbackStrength(3);
        setPierceLevel(Byte.MAX_VALUE);
    }

    public ExtractorProjectileEntity(EntityType<? extends ExtractorProjectileEntity> type, World worldIn) {
        super(type, worldIn);
        setDamage(0.5);
        setKnockbackStrength(3);
        setPierceLevel(Byte.MAX_VALUE);
    }


    @OnlyIn(Dist.CLIENT)
    public ExtractorProjectileEntity(World worldIn, double x, double y, double z) {
        super(type, x, y, z, worldIn);
        setDamage(0.5);
        setKnockbackStrength(3);
        setPierceLevel(Byte.MAX_VALUE);
    }

    public ExtractorProjectileEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
        super(type, worldIn);
        setDamage(0.5);
        setKnockbackStrength(3);
        setPierceLevel(Byte.MAX_VALUE);
    }

    @Override
    protected void registerData() {
        super.registerData();
    }

    @Override
    protected void onImpact(RayTraceResult rayTraceResult) {
        if (!world.isRemote
                && rayTraceResult.getType() == RayTraceResult.Type.BLOCK
                && getMotion().lengthSquared() > 0.95) {
            ServerPlayerEntity shooter = CastOptional.cast(func_234616_v_(), ServerPlayerEntity.class).orElse(null);
            BlockPos pos = ((BlockRayTraceResult) rayTraceResult).getPos();

            if (shooter != null && breakBlock(world, pos, shooter)) {
                breakAround(world, pos, ((BlockRayTraceResult) rayTraceResult).getFace(), shooter);
                setMotion(getMotion().scale(0.95f));
                hitAdditional();
                return;
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

    private void breakAround(World world, BlockPos pos, Direction face, ServerPlayerEntity shooter) {
        Vector3i axis1 = RotationHelper.shiftAxis(face.getDirectionVec());
        Vector3i axis2 = RotationHelper.shiftAxis(axis1);
        ServerScheduler.schedule(2, () -> {
                breakBlock(world, pos.add(axis1), shooter);
                breakBlock(world, pos.subtract(axis1), shooter);
        });
        ServerScheduler.schedule(4, () -> {
                breakBlock(world, pos.add(axis2), shooter);
                breakBlock(world, pos.subtract(axis2), shooter);
        });
        ServerScheduler.schedule(6, () -> {
                breakBlock(world, pos.add(axis1).add(axis2), shooter);
                breakBlock(world, pos.subtract(axis1).subtract(axis2), shooter);
        });
        ServerScheduler.schedule(8, () -> {
                breakBlock(world, pos.add(axis1).subtract(axis2), shooter);
                breakBlock(world, pos.subtract(axis1).add(axis2), shooter);
        });
    }

    private boolean breakBlock(World world, BlockPos pos, ServerPlayerEntity shooter) {
        ServerWorld serverWorld = (ServerWorld) world;
        GameType gameType = shooter.interactionManager.getGameType();
        BlockState blockState = world.getBlockState(pos);

        TileEntity tileEntity = world.getTileEntity(pos);

        if (blockState.getBlockHardness(world, pos) != -1
                && isAlive()
                && !shooter.blockActionRestricted(world, pos, gameType)
                && FracturedBedrockTile.breakMaterials.contains(blockState.getMaterial())
                && blockState.getBlock().removedByPlayer(blockState, world, pos, shooter, true, world.getFluidState(pos))
                && ForgeHooks.onBlockBreakEvent(world, gameType, shooter, pos) != -1) {

            blockState.getBlock().harvestBlock(world, shooter, pos, blockState, tileEntity, ItemStack.EMPTY);
            blockState.getBlock().onPlayerDestroy(world, pos, blockState);
            world.playEvent(null, 2001, pos, Block.getStateId(blockState));
            damage++;
            heat += 10;

            // custom exp drop check since player is not holding an item that can harvest the block
            int exp = blockState.getExpDrop(world, pos, 0, 0);
            if (exp > 0) {
                blockState.getBlock().dropXpOnBlockBreak(serverWorld, pos, exp);
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
        world.createExplosion(func_234616_v_(), getPosX(), getPosY(), getPosZ(), 4, Explosion.Mode.BREAK);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            if (isOnGround() && heat > 0) {
                int cooldown = isInWater() ? 10 : 1;
                if (ticksExisted % 10 == 0) {
                    Vector3d pos = getPositionVec().add(getLookVec().scale(-Math.random()));
                    ((ServerWorld) world).spawnParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, cooldown, 0,
                            0.01, 0, 0.01D);

                    ((ServerWorld) world).spawnParticle(ParticleTypes.FLAME, pos.x, pos.y + 0.1, pos.z, 1, 0,
                            0.01, 0, 0.01D);
                }

                if (cooldown > 1 && !extinguishing) {
                    world.playSound(null, getPosX(), getPosY(), getPosZ(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.2f, 0.9f);
                    ((ServerWorld) world).spawnParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, getPosX(), getPosY(), getPosZ(), 12, 0,
                            0.01, 0, 0.01D);

                    extinguishing = true;
                }

                heat -= cooldown;
            } else {
                if (ticksExisted % 40 == 0) {
                    Vector3d pos = getPositionVec().add(getLookVec().scale(-Math.random()));
                    ((ServerWorld) world).spawnParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 1, 0,
                            0.01, 0, 0.01D);
                }
            }
        }
    }

    @Override
    public boolean isOnGround() {
        return timeInGround > 0;
    }

    @Override
    protected ItemStack getArrowStack() {
        if (damage == 0) {
            return new ItemStack(ChthonicExtractorBlock.item);
        }

        ItemStack itemStack = new ItemStack(ChthonicExtractorBlock.usedItem);
        itemStack.setDamage(damage);
        return itemStack;
    }

    @Override
    protected void func_230299_a_(BlockRayTraceResult rayTraceResult) {
        super.func_230299_a_(rayTraceResult);
        this.setHitSound(SoundEvents.BLOCK_NETHERITE_BLOCK_HIT);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult p_213868_1_) {
        super.onEntityHit(p_213868_1_);
        setMotion(getMotion().normalize().scale(-0.1));
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void onCollideWithPlayer(PlayerEntity player) {
        if (inGround) {
            super.onCollideWithPlayer(player);

            // this should mean that it has been picked up
            if (!isAlive()) {
                ignitePlayer(player);
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    // pretty much the same as a regular pickup but attempts to place it in the offhand first
    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
        if (!world.isRemote
                && isOnGround()
                && isAlive()
                && pickupStatus == AbstractArrowEntity.PickupStatus.ALLOWED) {

            ItemStack itemStack = getArrowStack();
            boolean success = false;

            if (player.getHeldItemMainhand().isEmpty()) {
                player.setHeldItem(Hand.MAIN_HAND, itemStack);
                success = true;
            } else if (player.getHeldItemOffhand().isEmpty()) {
                player.setHeldItem(Hand.OFF_HAND, itemStack);
                success = true;
            } else if (player.inventory.addItemStackToInventory(itemStack)) {
                success = true;
            }

            if (success) {
                player.onItemPickup(this, 1);
                ignitePlayer(player);
                remove();

                return ActionResultType.SUCCESS;
            }
        }
        return super.applyPlayerInteraction(player, vec, hand);
    }

    private void ignitePlayer(PlayerEntity player) {
        if (!isAlive() && heat > 10) {
            player.setFire(3 + heat / 20);
        }
    }

    // possibly stops this from being removed after sitting around for too long
    @Override
    public void func_225516_i_() { }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        damage = compound.getInt(damageKey);
        heat = compound.getInt(heatKey);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        compound.putInt(damageKey, damage);
        compound.putInt(heatKey, heat);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(damage);
        buffer.writeInt(heat);
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        damage = buffer.readInt();
        heat = buffer.readInt();
    }
}
