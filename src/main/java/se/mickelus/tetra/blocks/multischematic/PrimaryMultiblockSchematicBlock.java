package se.mickelus.tetra.blocks.multischematic;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;
import org.joml.Vector3f;
import se.mickelus.mutil.util.RotationHelper;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.ICraftingEffectProviderBlock;
import se.mickelus.tetra.blocks.ISchematicProviderBlock;

public class PrimaryMultiblockSchematicBlock extends MultiblockSchematicBlock implements ISchematicProviderBlock, ICraftingEffectProviderBlock {
    public static final BooleanProperty complete = BooleanProperty.create("complete");

    protected final ResourceLocation[] schematics;

    public PrimaryMultiblockSchematicBlock(Properties properties, String schematic, RegistryObject<RuinedMultiblockSchematicBlock> ruinedRef,
            ResourceLocation pryTable, int x, int y, int height, int width) {
        super(properties, schematic, ruinedRef, pryTable, x, y, height, width);
        this.registerDefaultState(this.stateDefinition.any().setValue(facingProp, Direction.EAST).setValue(complete, false));

        this.schematics = new ResourceLocation[] { ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, schematic) };
    }

    @Override
    public boolean canUnlockSchematics(Level world, BlockPos pos, BlockPos targetPos) {
        return world.getBlockState(pos).getValue(complete);
    }

    @Override
    public ResourceLocation[] getSchematics(Level world, BlockPos pos, BlockState blockState) {
        return schematics;
    }

    @Override
    public boolean canUnlockCraftingEffects(Level world, BlockPos pos, BlockPos targetPos) {
        return world.getBlockState(pos).getValue(complete);
    }

    @Override
    public ResourceLocation[] getCraftingEffects(Level world, BlockPos pos, BlockState blockState) {
        return schematics;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(complete);
    }

    public void updateComplete(BlockState blockState, LevelAccessor level, BlockPos worldPos, BlockPos placePos) {
        boolean isComplete = getSchematicParts(blockState, level, worldPos)
                .filter(part -> part.blockState().getBlock() instanceof MultiblockSchematicBlock)
                .filter(part -> isCorrectPart(part.basePos(), (MultiblockSchematicBlock) part.blockState().getBlock()))
                .count() == width * height;
        level.setBlock(worldPos, blockState.setValue(complete, isComplete), Block.UPDATE_ALL);

        if (isComplete) {
            level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPos).inflate(10, 5, 10))
                    .forEach(player -> BlockUseCriterion.trigger(player, blockState, ItemStack.EMPTY, ImmutableMap.<String, String>builder().put("complete_schematic", "true").put("schematic", schematic).build()));
            spawnCompleteParticle(blockState, (ServerLevel) level, worldPos, placePos);
        }
    }

    protected void spawnCompleteParticle(BlockState blockState, ServerLevel level, BlockPos worldPos, BlockPos placePos) {
        Direction facing = blockState.getValue(facingProp);
        Vec3 face = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 dir = Vec3.atLowerCornerOf(facing.getCounterClockWise().getNormal());
        Vec3 placeVec = Vec3.atCenterOf(placePos);
        Vec3 origin = Vec3.atBottomCenterOf(RotationHelper.rotateDirection(new BlockPos(-x, -y, 0), facing).offset(worldPos))
                .add(face.scale(0.52))
                .add(dir.scale(-0.5));

        for (float x = 0; x < width; x += 0.333333333) {
            Vec3 pPos = origin.add(dir.scale(x)).add(0, Math.sin(System.currentTimeMillis() + 5 + y * 2.3) * 0.1, 0);
//            Vec3 pPos = origin.add(dir.scale(x));
            spawnParticle(level, pPos, getDelay(pPos, placeVec));
        }

        for (float x = 0; x < width; x += 0.333333333) {
            Vec3 pPos = origin.add(dir.scale(x)).add(0, height + Math.sin(System.currentTimeMillis() + 2 + y * 2.56) * 0.1, 0);
//            Vec3 pPos = origin.add(dir.scale(x)).add(0, height, 0);
            spawnParticle(level, pPos, getDelay(pPos, placeVec));
        }

        for (float y = 0; y < height; y += 0.333333333) {
            Vec3 pPos = origin.add(0, y, 0).add(dir.scale(Math.sin(System.currentTimeMillis() + 2 + y * 2.56) * 0.1));
//            Vec3 pPos = origin.add(0, y, 0);

            spawnParticle(level, pPos, getDelay(pPos, placeVec));
        }

        for (float y = 0; y < height; y += 0.333333333) {
            Vec3 pPos = origin.add(0, y, 0).add(dir.scale(width + Math.sin(System.currentTimeMillis() + 5 + y * 2.3) * 0.1));
//            Vec3 pPos = origin.add(0, y, 0).add(dir.scale(width));
            spawnParticle(level, pPos, getDelay(pPos, placeVec));
        }
    }

    private void spawnParticle(ServerLevel level, Vec3 pos, int delay) {
        ServerScheduler.schedule(delay, () ->
                level.sendParticles(new DustParticleOptions(new Vector3f(0.1f, 0.9f, 0.5f), 1f),
                        pos.x, pos.y, pos.z, 1, 0, 0, 0, 1));
    }

    private int getDelay(Vec3 a, Vec3 b) {
        return 2 * (width + height) + Math.max((int) (3 * (width + height - (Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z)))), 0);
    }

    protected boolean isCorrectPart(BlockPos basePos, MultiblockSchematicBlock block) {
        return schematic.equals(block.schematic) && block.x == basePos.getX() && block.y == basePos.getY();
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState oldBlock, boolean p_60570_) {
        if (!equals(oldBlock.getBlock())) {
            updateComplete(blockState, level, blockPos, blockPos);
        }
    }
}
