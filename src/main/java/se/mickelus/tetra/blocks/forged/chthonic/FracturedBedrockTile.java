package se.mickelus.tetra.blocks.forged.chthonic;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.extractor.SeepingBedrockBlock;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.util.ITetraTicker;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Set;
@ParametersAreNonnullByDefault
public class FracturedBedrockTile extends BlockEntity implements ITetraTicker {
    @ObjectHolder(TetraMod.MOD_ID + ":" + FracturedBedrockBlock.unlocalizedName)
    public static BlockEntityType<FracturedBedrockTile> type;

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

    private MobSpawnSettings spawnInfo;

    public FracturedBedrockTile(BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void updateLuck() {
        if (spawnInfo == null) {
            spawnInfo = level.getBiome(worldPosition).getMobSettings();
        }

        boolean spawnBonus = spawnInfo.getMobs(MobCategory.MONSTER).unwrap().stream()
                .map(spawner -> spawner.type)
                .anyMatch(type -> EntityType.HUSK.equals(type) || EntityType.STRAY.equals(type) || EntityType.WITCH.equals(type));
        if (spawnBonus) {
            luck += 1;
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
        return 20 - Mth.clamp(activity / 64 * 5, 0, 15);
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

    private Vec3 getTarget(int i) {
        int maxDistance = getMaxDistance();
        int steps = 32;
        double directionRotation = 90d * (i % 4);
        double offsetRotation = 360d / steps * (i / 4) + i / 8f;
        float pitch = -(i % (steps * 16)) / steps * 5f;

        return Vec3.directionFromRotation(pitch, (float) (directionRotation + offsetRotation)).multiply(maxDistance, 4 + maxDistance / 2f, maxDistance);
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

    // todo 1.18: changed significantly, verify functionality
    private BlockHitResult raytrace(Level level, Vec3 origin, Vec3 target) {
        return BlockGetter.traverseBlocks(origin, target, new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null), (ctx, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (isBedrock(block) || canReplace(blockState)) {
                return null;
            }

            VoxelShape voxelshape = ctx.getBlockShape(blockState, level, blockPos);
            return level.clipWithInteractionOverride(origin, target, blockPos, voxelshape, blockState);
        }, ctx -> {
            Vec3 vec3 = ctx.getFrom().subtract(ctx.getTo());
            return BlockHitResult.miss(ctx.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(ctx.getTo()));
        });
    }

    private BlockPos traceDown(BlockPos blockPos, Level level) {
        BlockPos.MutableBlockPos movePos = blockPos.mutable();
        int minY = level.getMinBuildHeight();

        while (movePos.getY() > minY) {
            movePos.move(Direction.DOWN);
            BlockState blockState = level.getBlockState(movePos);

            if (isBedrock(blockState.getBlock())) {
                return movePos.move(Direction.UP).immutable();
            }

            if (!blockState.isAir()) {
                return movePos.immutable();
            }
        }

        return movePos.immutable();
    }

    public static boolean breakBlock(Level world, BlockPos pos, BlockState blockState) {
        if (world instanceof ServerLevel serverLevel
                && !blockState.isAir()
                && breakMaterials.contains(blockState.getMaterial())
                && blockState.getDestroySpeed(world, pos) > -1) {
            BlockEntity tile = blockState.getBlock() instanceof EntityBlock ? world.getBlockEntity(pos) : null;
            LootContext.Builder lootBuilder = new LootContext.Builder(serverLevel)
                    .withRandom(world.random)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, tile)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, null);

            blockState.getDrops(lootBuilder).forEach(itemStack -> Block.popResource(world, pos, itemStack));

            world.levelEvent(null, 2001, pos, Block.getId(world.getBlockState(pos)));
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

            return true;
        }

        return false;
    }

    private void spawnOre(BlockPos pos) {
        ServerLevel serverWorld = (ServerLevel) level;
        LootTable table = serverWorld.getServer().getLootTables().get(lootTables[getTier()]);
        LootContext context = new LootContext.Builder(serverWorld).withLuck(luck).create(LootContextParamSets.EMPTY);

        table.getRandomItems(context).stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .findAny()
                .ifPresent(itemStack -> {
                    if (itemStack.getItem() instanceof BlockItem blockItem) {
                        BlockState newState = blockItem.getBlock().defaultBlockState();
                        level.levelEvent(null, 2001, pos, Block.getId(newState));
                        level.setBlock(pos, newState, 2);
                    } else {
                        Block.popResource(level, pos, itemStack);
                    }
                });
    }

    /**
     * Spawns one of the biomes "monster type" mobs at the given location, based on {@link net.minecraft.world.level.NaturalSpawner}
     * @param pos
     */
    private void spawnMob(BlockPos pos) {
        if (spawnInfo == null) {
            spawnInfo = level.getBiome(pos).getMobSettings();
        }

        if (getBlockPos().distSqr(pos) < 42 && level.getRandom().nextFloat() >= spawnInfo.getCreatureProbability() / 5) {
            return;
        }

        WeightedRandomList<MobSpawnSettings.SpawnerData> spawners = spawnInfo.getMobs(MobCategory.MONSTER);
        Optional<MobSpawnSettings.SpawnerData> optionalSpawnerData = spawners.getRandom(level.getRandom());
        if (!optionalSpawnerData.isPresent())
            return;
        MobSpawnSettings.SpawnerData mob = optionalSpawnerData.get();

        ServerLevel serverWorld = (ServerLevel) level;
        if (mob.type.canSummon()
//                && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementType(mob.type), world, pos, mob.type)
                && serverWorld.noCollision(mob.type.getAABB(pos.getX(), pos.getY(), pos.getZ()))
                && SpawnPlacements.checkSpawnRules(mob.type, serverWorld, MobSpawnType.SPAWNER, pos, serverWorld.getRandom())) {

            Entity entity;
            try {
                entity = mob.type.create(serverWorld.getLevel());
            } catch (Exception exception) {
                logger.warn("Failed to create mob", exception);
                return;
            }

            entity.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            CastOptional.cast(entity, Mob.class)
                    .filter(e -> ForgeHooks.canEntitySpawn(e, serverWorld, pos.getX(), pos.getY(), pos.getZ(), null, MobSpawnType.SPAWNER) != -1)
                    .filter(e -> e.checkSpawnRules(serverWorld, MobSpawnType.CHUNK_GENERATION))
                    .filter(e -> e.checkSpawnObstruction(serverWorld))
                    .ifPresent(e -> {
                        e.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(e.blockPosition()), MobSpawnType.CHUNK_GENERATION, null, null);
                        serverWorld.addFreshEntityWithPassengers(e);

                        // makes the mob angry at a nearby player
                        serverWorld.getEntitiesOfClass(Player.class, new AABB(getBlockPos()).inflate(24, 8, 24)).stream()
                                .findAny()
                                .ifPresent(e::setLastHurtMob);
                    });
        }
    }

    private void playSound() {
        level.playSound(null, worldPosition.below(worldPosition.getY()), SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 3f, 0.5f);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

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
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        compound.putInt(activityKey, activity);
        compound.putInt(stepKey, step);
        compound.putInt(luckKey, luck);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.load(packet.getTag());
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide && activity > 0 && level.getGameTime() % getRate() == 0) {
            int intensity = getIntensity();
            Vec3 origin = Vec3.atCenterOf(pos);

            for (int i = 0; i < intensity; i++) {
                Vec3 target = getTarget(step + i);
                BlockHitResult result = raytrace(level, origin, origin.add(target));

                if (result.getType() != HitResult.Type.MISS) {
                    BlockPos hitPos = result.getBlockPos();
                    BlockState hitState = level.getBlockState(hitPos);

                    breakBlock(level, hitPos, hitState);

                    BlockPos spawnPos = traceDown(hitPos, level);
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

            ((ServerLevel) level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FracturedBedrockBlock.instance.defaultBlockState()),
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5,
                8, 0, level.random.nextGaussian() * 0.1, 0, 0.1);

            step += intensity;
            activity -= intensity;

            if (shouldDeplete()) {
                level.setBlock(getBlockPos(), DepletedBedrockBlock.instance.defaultBlockState(), 2);
            }
        }

        if (!level.isClientSide  && activity > 0 && this.level.getGameTime() % 80 == 0) {
            playSound();
        }
    }
}
