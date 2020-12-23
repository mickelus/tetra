package se.mickelus.tetra.blocks.forged.chthonic;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.extractor.SeepingBedrockBlock;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class FracturedBedrockTile extends TileEntity implements ITickableTileEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + FracturedBedrockBlock.unlocalizedName)
    public static TileEntityType<FracturedBedrockTile> type;

    private static final Logger logger = LogManager.getLogger();

    private static final String activityKey = "actv";
    private int activity = 0;

    private static final String stepKey = "step";
    private int step = 0;

    private float spawnRatio = 0.5f;
    private int spawnYLimit = 4;

    public static final Set<Material> breakMaterials = Sets.newHashSet(Material.ROCK, Material.CLAY, Material.EARTH);

    private static final String luckKey = "luck";
    private int luck = 0;

    private static final ResourceLocation[] lootTables = new ResourceLocation[] {
            new ResourceLocation(TetraMod.MOD_ID, "extractor/tier1"),
            new ResourceLocation(TetraMod.MOD_ID, "extractor/tier2"),
            new ResourceLocation(TetraMod.MOD_ID, "extractor/tier3"),
            new ResourceLocation(TetraMod.MOD_ID, "extractor/tier4")
    };

    private MobSpawnInfo spawnInfo;

    public FracturedBedrockTile() {
        super(type);
    }

    public void updateLuck(boolean wasSeeping) {
        if (spawnInfo == null) {
            spawnInfo = world.getBiome(pos).getMobSpawnInfo();
        }

        boolean spawnBonus = spawnInfo.getSpawners(EntityClassification.MONSTER).stream()
                .map(spawner -> spawner.type)
                .anyMatch(type -> EntityType.HUSK.equals(type) || EntityType.STRAY.equals(type) || EntityType.WITCH.equals(type));
        if (spawnBonus) {
            luck += 1;
        }

        if (wasSeeping) {
            luck += 2;
        }
    }

    public void activate(int amount) {
        if (!world.isRemote && activity <= 0) {
            playSound();
        }

        int preTier = getProjectedTier();

        activity += amount;
        markDirty();

        if (!world.isRemote && getProjectedTier() != preTier) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
        }
    }

    private int getRate() {
        return 20 - MathHelper.clamp(activity / 64 * 5, 0, 15);
    }

    private int getIntensity() {
        return Math.min(1 + activity / 16, 4);
    }

    public int getProjectedTier() {
        return getTier(step + activity);
    }

    private int getTier() {
        return getTier(step);
    }

    private int getTier(int ref) {
        if (ref > ChthonicExtractorBlock.maxDamage * 10) {
            return 3;
        } else if (ref > ChthonicExtractorBlock.maxDamage * 4) {
            return 2;
        } else if (ref > ChthonicExtractorBlock.maxDamage) {
            return 1;
        }
        return 0;
    }

    private int getMaxDistance() {
        switch (getTier()) {
            case 0:
                return 12;
            case 1:
                return 16;
            case 2:
                return 20;
            case 3:
                return 25;
        }

        return 25;
    }

    private boolean shouldDeplete() {
        return step >= ChthonicExtractorBlock.maxDamage * 12;
    }

    private Vector3d getTarget(int i) {
        int maxDistance = getMaxDistance();
        int steps = 32;
        double directionRotation = 90d * (i % 4);
        double offsetRotation = 360d / steps * (i / 4) + i / 8f;
        float pitch = -(i % (steps * 16)) / steps * 5f;

        return Vector3d.fromPitchYaw(pitch, (float) (directionRotation + offsetRotation)).mul(maxDistance, 4 + maxDistance / 2f, maxDistance);
    }

    private boolean isBedrock(Block block) {
        return Blocks.BEDROCK.equals(block)
                || FracturedBedrockBlock.instance.equals(block)
                || SeepingBedrockBlock.instance.equals(block)
                || ChthonicExtractorBlock.instance.equals(block);
    }

    private boolean canReplace(BlockState blockState) {
        return blockState.getMaterial().isReplaceable();
    }

    private BlockPos raytrace(Vector3d origin, Vector3d target) {
        return IBlockReader.doRayTrace(new RayTraceContext(origin, target, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null), (ctx, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (isBedrock(block) || canReplace(blockState)) {
                return null;
            }
            return blockPos.toImmutable();
        }, ctx -> null);
    }

    private BlockPos traceDown(BlockPos blockPos) {
        BlockPos.Mutable movePos = blockPos.toMutable();

        while (movePos.getY() >= 0) {
            movePos.move(Direction.DOWN);
            BlockState blockState = world.getBlockState(movePos);

            if (isBedrock(blockState.getBlock())) {
                return movePos.move(Direction.UP).toImmutable();
            }

            if (!blockState.isAir(world, movePos)) {
                return movePos.toImmutable();
            }
        }

        return movePos.toImmutable();
    }

    public static boolean breakBlock(World world, BlockPos pos, BlockState blockState) {
        if (world instanceof ServerWorld
                && !blockState.isAir(world, pos)
                && breakMaterials.contains(blockState.getMaterial())
                && blockState.getBlockHardness(world, pos) > -1) {
            TileEntity tile = blockState.hasTileEntity() ? world.getTileEntity(pos) : null;
            LootContext.Builder lootBuilder = new LootContext.Builder((ServerWorld) world)
                    .withRandom(world.rand)
                    .withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(pos))
                    .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                    .withNullableParameter(LootParameters.BLOCK_ENTITY, tile)
                    .withNullableParameter(LootParameters.THIS_ENTITY, null);

            blockState.getDrops(lootBuilder).forEach(itemStack -> Block.spawnAsEntity(world, pos, itemStack));

            world.playEvent(null, 2001, pos, Block.getStateId(world.getBlockState(pos)));
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

            return true;
        }

        return false;
    }

    private void spawnOre(BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld) world;
        LootTable table = serverWorld.getServer().getLootTableManager().getLootTableFromLocation(lootTables[getTier()]);
        LootContext context = new LootContext.Builder(serverWorld).withLuck(luck).build(LootParameterSets.EMPTY);

        table.generate(context).stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .findAny()
                .ifPresent(itemStack -> {
                    if (itemStack.getItem() instanceof BlockItem) {
                        BlockState newState = ((BlockItem) itemStack.getItem()).getBlock().getDefaultState();
                        world.playEvent(null, 2001, pos, Block.getStateId(newState));
                        world.setBlockState(pos, newState, 2);
                    } else {
                        Block.spawnAsEntity(world, pos, itemStack);
                    }
                });
    }

    /**
     * Spawns one of the biomes "monster type" mobs at the given location, based on {@link WorldEntitySpawner#performWorldGenSpawning}
     * @param pos
     */
    private void spawnMob(BlockPos pos) {
        if (spawnInfo == null) {
            spawnInfo = world.getBiome(pos).getMobSpawnInfo();
        }

        if (getPos().distanceSq(pos) > 36 && world.getRandom().nextFloat() >= spawnInfo.getCreatureSpawnProbability()) {
            return;
        }

        List<MobSpawnInfo.Spawners> spawners = spawnInfo.getSpawners(EntityClassification.MONSTER);

        if (WeightedRandom.getTotalWeight(spawners) == 0) {
            return;
        }

        MobSpawnInfo.Spawners mob = WeightedRandom.getRandomItem(world.getRandom(), spawners);

        ServerWorld serverWorld = (ServerWorld) world;
        if (mob.type.isSummonable()
//                && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementType(mob.type), world, pos, mob.type)
                && serverWorld.hasNoCollisions(mob.type.getBoundingBoxWithSizeApplied(pos.getX(), pos.getY(), pos.getZ()))
                && EntitySpawnPlacementRegistry.canSpawnEntity(mob.type, serverWorld, SpawnReason.SPAWNER, pos, serverWorld.getRandom())) {

            Entity entity;
            try {
                entity = mob.type.create(serverWorld.getWorld());
            } catch (Exception exception) {
                logger.warn("Failed to create mob", exception);
                return;
            }

            entity.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            CastOptional.cast(entity, MobEntity.class)
                    .filter(e -> ForgeHooks.canEntitySpawn(e, serverWorld, pos.getX(), pos.getY(), pos.getZ(), null, SpawnReason.SPAWNER) != -1)
                    .filter(e -> e.canSpawn(serverWorld, SpawnReason.CHUNK_GENERATION))
                    .filter(e -> e.isNotColliding(serverWorld))
                    .ifPresent(e -> {
                        e.onInitialSpawn(serverWorld, serverWorld.getDifficultyForLocation(e.getPosition()), SpawnReason.CHUNK_GENERATION, null, null);
                        serverWorld.func_242417_l(e);

                        // makes the mob angry at a nearby player
                        serverWorld.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(getPos()).grow(24, 8, 24)).stream()
                                .findAny()
                                .ifPresent(e::setLastAttackedEntity);
                    });
        }
    }

    private void playSound() {
        world.playSound(null, pos.down(pos.getY()), SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 3f, 0.5f);
    }

    @Override
    public void tick() {
        if (!world.isRemote && activity > 0 && world.getGameTime() % getRate() == 0) {
            int intensity = getIntensity();
            Vector3d origin = Vector3d.copyCentered(getPos());

            for (int i = 0; i < intensity; i++) {
                Vector3d target = getTarget(step + i);
                BlockPos hitPos = raytrace(origin, origin.add(target));

                if (hitPos != null) {
                    BlockState blockState = world.getBlockState(hitPos);

                    breakBlock(world, hitPos, blockState);

                    BlockPos spawnPos = traceDown(hitPos);
                    BlockState spawnState = world.getBlockState(spawnPos);

                    if (canReplace(spawnState)) {
                        if (world.getRandom().nextFloat() < spawnRatio) {
                            if (spawnPos.getY() < spawnYLimit) {
                                spawnOre(spawnPos);
                            }
                        } else {
                            spawnMob(spawnPos);
                        }
                    } else {
                        breakBlock(world, spawnPos, spawnState);
                    }
                }
            }

            ((ServerWorld) world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, FracturedBedrockBlock.instance.getDefaultState()),
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    8, 0, world.rand.nextGaussian() * 0.1, 0, 0.1);

            step += intensity;
            activity -= intensity;

            if (shouldDeplete()) {
                world.setBlockState(getPos(), DepletedBedrockBlock.instance.getDefaultState(), 2);
            }
        }

        if (!world.isRemote  && activity > 0 && world.getGameTime() % 80 == 0) {
            playSound();
        }
    }

    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);

        if (compound.contains(activityKey)) {
            activity = compound.getInt(activityKey);
        }

        if (compound.contains(stepKey)) {
            step = compound.getInt(stepKey);
        }

        if (compound.contains(luckKey)) {
            luck = compound.getInt(luckKey);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putInt(activityKey, activity);
        compound.putInt(stepKey, step);
        compound.putInt(luckKey, luck);

        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.read(getBlockState(), packet.getNbtCompound());
    }
}
