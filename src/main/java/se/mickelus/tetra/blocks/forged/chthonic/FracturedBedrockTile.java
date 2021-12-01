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

    public static final Set<Material> breakMaterials = Sets.newHashSet(Material.STONE, Material.CLAY, Material.DIRT);

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
            spawnInfo = level.getBiome(worldPosition).getMobSettings();
        }

        boolean spawnBonus = spawnInfo.getMobs(EntityClassification.MONSTER).stream()
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
        if (!level.isClientSide && activity <= 0) {
            playSound();
        }

        int preTier = getProjectedTier();

        activity += amount;
        setChanged();

        if (!level.isClientSide && getProjectedTier() != preTier) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

        return Vector3d.directionFromRotation(pitch, (float) (directionRotation + offsetRotation)).multiply(maxDistance, 4 + maxDistance / 2f, maxDistance);
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
        return IBlockReader.traverseBlocks(new RayTraceContext(origin, target, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null), (ctx, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (isBedrock(block) || canReplace(blockState)) {
                return null;
            }
            return blockPos.immutable();
        }, ctx -> null);
    }

    private BlockPos traceDown(BlockPos blockPos) {
        BlockPos.Mutable movePos = blockPos.mutable();

        while (movePos.getY() >= 0) {
            movePos.move(Direction.DOWN);
            BlockState blockState = level.getBlockState(movePos);

            if (isBedrock(blockState.getBlock())) {
                return movePos.move(Direction.UP).immutable();
            }

            if (!blockState.isAir(level, movePos)) {
                return movePos.immutable();
            }
        }

        return movePos.immutable();
    }

    public static boolean breakBlock(World world, BlockPos pos, BlockState blockState) {
        if (world instanceof ServerWorld
                && !blockState.isAir(world, pos)
                && breakMaterials.contains(blockState.getMaterial())
                && blockState.getDestroySpeed(world, pos) > -1) {
            TileEntity tile = blockState.hasTileEntity() ? world.getBlockEntity(pos) : null;
            LootContext.Builder lootBuilder = new LootContext.Builder((ServerWorld) world)
                    .withRandom(world.random)
                    .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pos))
                    .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                    .withOptionalParameter(LootParameters.BLOCK_ENTITY, tile)
                    .withOptionalParameter(LootParameters.THIS_ENTITY, null);

            blockState.getDrops(lootBuilder).forEach(itemStack -> Block.popResource(world, pos, itemStack));

            world.levelEvent(null, 2001, pos, Block.getId(world.getBlockState(pos)));
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

            return true;
        }

        return false;
    }

    private void spawnOre(BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld) level;
        LootTable table = serverWorld.getServer().getLootTables().get(lootTables[getTier()]);
        LootContext context = new LootContext.Builder(serverWorld).withLuck(luck).create(LootParameterSets.EMPTY);

        table.getRandomItems(context).stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .findAny()
                .ifPresent(itemStack -> {
                    if (itemStack.getItem() instanceof BlockItem) {
                        BlockState newState = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
                        level.levelEvent(null, 2001, pos, Block.getId(newState));
                        level.setBlock(pos, newState, 2);
                    } else {
                        Block.popResource(level, pos, itemStack);
                    }
                });
    }

    /**
     * Spawns one of the biomes "monster type" mobs at the given location, based on {@link WorldEntitySpawner#performWorldGenSpawning}
     * @param pos
     */
    private void spawnMob(BlockPos pos) {
        if (spawnInfo == null) {
            spawnInfo = level.getBiome(pos).getMobSettings();
        }

        if (getBlockPos().distSqr(pos) < 42 && level.getRandom().nextFloat() >= spawnInfo.getCreatureProbability() / 5) {
            return;
        }

        List<MobSpawnInfo.Spawners> spawners = spawnInfo.getMobs(EntityClassification.MONSTER);

        if (WeightedRandom.getTotalWeight(spawners) == 0) {
            return;
        }

        MobSpawnInfo.Spawners mob = WeightedRandom.getRandomItem(level.getRandom(), spawners);

        ServerWorld serverWorld = (ServerWorld) level;
        if (mob.type.canSummon()
//                && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementType(mob.type), world, pos, mob.type)
                && serverWorld.noCollision(mob.type.getAABB(pos.getX(), pos.getY(), pos.getZ()))
                && EntitySpawnPlacementRegistry.checkSpawnRules(mob.type, serverWorld, SpawnReason.SPAWNER, pos, serverWorld.getRandom())) {

            Entity entity;
            try {
                entity = mob.type.create(serverWorld.getLevel());
            } catch (Exception exception) {
                logger.warn("Failed to create mob", exception);
                return;
            }

            entity.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            CastOptional.cast(entity, MobEntity.class)
                    .filter(e -> ForgeHooks.canEntitySpawn(e, serverWorld, pos.getX(), pos.getY(), pos.getZ(), null, SpawnReason.SPAWNER) != -1)
                    .filter(e -> e.checkSpawnRules(serverWorld, SpawnReason.CHUNK_GENERATION))
                    .filter(e -> e.checkSpawnObstruction(serverWorld))
                    .ifPresent(e -> {
                        e.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(e.blockPosition()), SpawnReason.CHUNK_GENERATION, null, null);
                        serverWorld.addFreshEntityWithPassengers(e);

                        // makes the mob angry at a nearby player
                        serverWorld.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB(getBlockPos()).inflate(24, 8, 24)).stream()
                                .findAny()
                                .ifPresent(e::setLastHurtMob);
                    });
        }
    }

    private void playSound() {
        level.playSound(null, worldPosition.below(worldPosition.getY()), SoundEvents.BEACON_AMBIENT, SoundCategory.BLOCKS, 3f, 0.5f);
    }

    @Override
    public void tick() {
        if (!level.isClientSide && activity > 0 && level.getGameTime() % getRate() == 0) {
            int intensity = getIntensity();
            Vector3d origin = Vector3d.atCenterOf(getBlockPos());

            for (int i = 0; i < intensity; i++) {
                Vector3d target = getTarget(step + i);
                BlockPos hitPos = raytrace(origin, origin.add(target));

                if (hitPos != null) {
                    BlockState blockState = level.getBlockState(hitPos);

                    breakBlock(level, hitPos, blockState);

                    BlockPos spawnPos = traceDown(hitPos);
                    BlockState spawnState = level.getBlockState(spawnPos);

                    if (canReplace(spawnState)) {
                        if (level.getRandom().nextFloat() < spawnRatio) {
                            if (spawnPos.getY() < spawnYLimit) {
                                spawnOre(spawnPos);
                            }
                        } else {
                            spawnMob(spawnPos);
                        }
                    } else {
                        breakBlock(level, spawnPos, spawnState);
                    }
                }
            }

            ((ServerWorld) level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, FracturedBedrockBlock.instance.defaultBlockState()),
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5,
                    8, 0, level.random.nextGaussian() * 0.1, 0, 0.1);

            step += intensity;
            activity -= intensity;

            if (shouldDeplete()) {
                level.setBlock(getBlockPos(), DepletedBedrockBlock.instance.defaultBlockState(), 2);
            }
        }

        if (!level.isClientSide  && activity > 0 && level.getGameTime() % 80 == 0) {
            playSound();
        }
    }

    @Override
    public void load(BlockState blockState, CompoundNBT compound) {
        super.load(blockState, compound);

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
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        compound.putInt(activityKey, activity);
        compound.putInt(stepKey, step);
        compound.putInt(luckKey, luck);

        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.load(getBlockState(), packet.getTag());
    }
}
