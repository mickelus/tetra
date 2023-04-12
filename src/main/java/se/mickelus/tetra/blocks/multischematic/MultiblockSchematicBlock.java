package se.mickelus.tetra.blocks.multischematic;

import com.mojang.math.Vector3f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import se.mickelus.mutil.util.RotationHelper;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.effect.EffectHelper;

import java.util.Collection;
import java.util.stream.Stream;

public class MultiblockSchematicBlock extends HorizontalDirectionalBlock implements IInteractiveBlock {
    public static final DirectionProperty facingProp = BlockStateProperties.HORIZONTAL_FACING;
    public final int x;
    public final int y;
    public final int height;
    public final int width;
    public final RegistryObject<RuinedMultiblockSchematicBlock> ruinedRef;
    protected String schematic;
    protected ResourceLocation pryTable;
    protected BlockInteraction[] pryAction = new BlockInteraction[]{
            new BlockInteraction(TetraToolActions.pry, 1, Direction.EAST, 6, 10, 7, 10,
                    BlockStatePredicate.ANY,
                    this::pryBlock)
    };

    public MultiblockSchematicBlock(Properties properties, String schematic, RegistryObject<RuinedMultiblockSchematicBlock> ruinedRef,
            @Nullable ResourceLocation pryTable, int x, int y, int height, int width) {
        super(properties);
        this.schematic = schematic;
        this.ruinedRef = ruinedRef;
        this.pryTable = pryTable;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;

        this.registerDefaultState(this.stateDefinition.any().setValue(facingProp, Direction.EAST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facingProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(facingProp, context.getHorizontalDirection().getOpposite());
    }


    protected Stream<Part> getSchematicParts(BlockState blockState, LevelAccessor level, BlockPos blockPos) {
        Direction dir = blockState.getValue(facingProp);
        AABB baseBox = new AABB(0, 0, 0, width - 1, height - 1, 0);
        return BlockPos.betweenClosedStream(baseBox)
                .map(pos -> {
                    BlockPos worldPos = RotationHelper.rotateDirection(pos.offset(-x, -y, 0), dir).offset(blockPos);
                    return new Part(pos.immutable(), worldPos, level.getBlockState(worldPos));
                });
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState oldBlock, boolean p_60570_) {
        super.onPlace(blockState, level, blockPos, oldBlock, p_60570_);
        notifyPrimary(level, blockPos, blockState);

    }

    @Override
    public void destroy(LevelAccessor level, BlockPos blockPos, BlockState blockState) {
        super.destroy(level, blockPos, blockState);
        if (!level.isClientSide()) {
            notifyPrimary(level, blockPos, blockState);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, entity, itemStack);
        if (level.isClientSide()) {
            spawnParticles(blockState, (ClientLevel) level, blockPos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (pryTable != null) {
            return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolAction> tools) {
        if (pryTable != null && face.getOpposite().equals(blockState.getValue(facingProp))) {
            return pryAction;
        }
        return new BlockInteraction[0];
    }

    protected boolean pryBlock(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction facing) {
        boolean didBreak = EffectHelper.breakBlock(world, player, player.getItemInHand(hand), pos, blockState, false);
        if (didBreak && world instanceof ServerLevel) {
            BlockInteraction.getLoot(pryTable, player, hand, (ServerLevel) world, blockState)
                    .forEach(lootStack -> popResource(world, pos, lootStack));
        }

        return true;
    }

    protected void notifyPrimary(LevelAccessor level, BlockPos blockPos, BlockState blockState) {
        getSchematicParts(blockState, level, blockPos)
                .filter(part -> part.blockState.getBlock() instanceof PrimaryMultiblockSchematicBlock)
                .forEach(part ->
                        ((PrimaryMultiblockSchematicBlock) part.blockState.getBlock()).updateComplete(part.blockState(), level, part.worldPos(), blockPos));
    }

    protected void spawnParticles(BlockState blockState, ClientLevel level, BlockPos blockPos) {
        Vec3 face = Vec3.atLowerCornerOf(blockState.getValue(facingProp).getNormal());
        Vec3 dir = Vec3.atLowerCornerOf(blockState.getValue(facingProp).getClockWise().getNormal());
        getSchematicParts(blockState, level, blockPos).forEach(part ->
                ServerScheduler.schedule(blockPos.distManhattan(part.worldPos) * 2, () ->
                        spawnParticleBlock(level, blockState, part.basePos(), part.blockState(), part.worldPos(), face, dir)));
    }

    protected void spawnParticleBlock(ClientLevel level, BlockState originState, BlockPos basePos, BlockState blockState, BlockPos pos, Vec3 face, Vec3 dir) {
        Vec3 facePos = Vec3.atCenterOf(pos).add(face.scale(0.52));
        DustParticleOptions particle;
        if (blockState.getBlock() instanceof MultiblockSchematicBlock block && block.x == basePos.getX() && block.y == basePos.getY()) {
            particle = new DustParticleOptions(new Vector3f(0.1f, 0.9f, 0.5f), 1f);
        } else {
            particle = new DustParticleOptions(new Vector3f(0.9f, 0.3f, 0.3f), 1f);
        }

        spawnParticle(level, particle, facePos);
    }

    protected void spawnParticle(ClientLevel level, DustParticleOptions particle, Vec3 pos) {
        level.addParticle(particle, pos.x, pos.y, pos.z, 0, 0, 0);
    }

    record Part(BlockPos basePos, BlockPos worldPos, BlockState blockState) {
    }

    public static class Builder {
        public static final String format = "%s_%d_%d";
        public static final String ruinedFormat = "%s_ruined_%d_%d";
        public static final String pryTablePrefix = "actions/forged_schematic/";
        private String identifier;
        private int height;
        private int width;

        private Properties properties;
        private Properties ruinedProperties;

        public Builder(String identifier, int width, int height, Properties properties) {
            this.identifier = identifier;
            this.width = width;
            this.height = height;

            this.properties = properties;
            ruinedProperties = properties;
        }

        public Builder withRuinedProperties(Properties properties) {
            this.ruinedProperties = properties;
            return this;
        }

        public void build(DeferredRegister<Block> blocks, DeferredRegister<Item> items) {
            if (FMLEnvironment.dist.isClient()) {
                MultiblockSchematicScrollHandler.setupSchematic(identifier, width * height);
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int x = i;
                    int y = j;


                    String ruinedId = String.format(ruinedFormat, identifier, x, y);
                    ResourceLocation brokenPryTable = new ResourceLocation("tetra", pryTablePrefix + ruinedId);
                    RegistryObject<RuinedMultiblockSchematicBlock> ruinedRef = blocks.register(ruinedId, () -> new RuinedMultiblockSchematicBlock(ruinedProperties, brokenPryTable));

                    String id = String.format(format, identifier, x, y);
                    ResourceLocation pryTable = new ResourceLocation("tetra", pryTablePrefix + id);
                    RegistryObject<MultiblockSchematicBlock> ref = x == width / 2 && y == height / 2
                            ? blocks.register(id, () -> new PrimaryMultiblockSchematicBlock(properties, identifier, ruinedRef, pryTable, x, y, height, width))
                            : blocks.register(id, () -> new MultiblockSchematicBlock(properties, identifier, ruinedRef, pryTable, x, y, height, width));


                    items.register(id, () -> {
                        StackedMultiblockSchematicItem item = new StackedMultiblockSchematicItem(ref.get(), ruinedRef.get());
                        if (FMLEnvironment.dist.isClient()) {
                            MultiblockSchematicScrollHandler.addSchematic(identifier, y * width + x, item);
                        }
                        return item;
                    });
                    items.register(ruinedId, () -> new RuinedMultiblockSchematicItem(ruinedRef.get(), ref.get()));

                }
            }
        }
    }
}
