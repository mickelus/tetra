package se.mickelus.tetra.blocks.forged;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.block.Block;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.Materials;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class BlockForgedVent extends TetraBlock implements IBlockCapabilityInteractive {

    static final String unlocalizedName = "forged_vent";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedVent instance;

    public static final PropertyInteger propRotation = PropertyInteger.create("rotation", 0, 3);
    public static final PropertyBool propX = PropertyBool.create("x");
    public static final PropertyBool propBroken = PropertyBool.create("broken");

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 1, 4, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(0)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 1, 4, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(1)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 12, 15, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(2)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 12, 15, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(3)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),

            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 12, 15, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(0)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 12, 15, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(1)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 1, 4, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(2)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 1, 4, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(3)),
                    (world, pos, blockState, player, hand, facing) -> breakBolt(world, pos, blockState, player, hand, facing)),

            new BlockInteraction(Capability.pry, 1, Direction.EAST, 7, 11, 8, 12,
                    new PropertyMatcher().where(propBroken, equalTo(true)),
                    (world, pos, blockState, player, hand, facing) -> breakPlate(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.pry, 1, Direction.WEST, 7, 11, 8, 12,
                    new PropertyMatcher().where(propBroken, equalTo(true)),
                    (world, pos, blockState, player, hand, facing) -> breakPlate(world, pos, blockState, player, hand, facing)),
    };

    private static final ResourceLocation boltLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/bolt_break");
    private static final ResourceLocation ventLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/vent_break");

    public BlockForgedVent() {
        super(Materials.forged);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());
        setBlockUnbreakable();
        setResistance(22);

        hasItem = true;

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(propRotation, 0)
                .withProperty(propX, true)
                .withProperty(propBroken, false));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        drops.clear();
    }

    private static boolean breakBolt(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {
        world.setBlockState(pos, world.getBlockState(pos).withProperty(propBroken, true), 2);

        if (!world.isRemote) {
            WorldServer worldServer = (WorldServer) world;
            LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(boltLootTable);
            LootContext.Builder builder = new LootContext.Builder(worldServer);
            builder.withLuck(player.getLuck()).withPlayer(player);

            table.generateLootForPools(player.getRNG(), builder.build()).forEach(itemStack -> {
                if (!player.inventory.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false);
                }
            });

            worldServer.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1, 0.5f);
        }

        return true;
    }

    private static boolean breakPlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {
        List<BlockPos> connectedVents = getConnectedBlocks(world, pos, new LinkedList<>(), blockState.getValue(propX));

        if (connectedVents.stream().anyMatch(blockPos -> !world.getBlockState(blockPos).getValue(propBroken))) {
            return false;
        }

        connectedVents.forEach(blockPos -> {
            world.playEvent(null, 2001, blockPos, Block.getStateId(world.getBlockState(blockPos)));
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2);
        });

        if (!world.isRemote) {
            WorldServer worldServer = (WorldServer) world;
            LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(ventLootTable);
            LootContext.Builder builder = new LootContext.Builder(worldServer);
            builder.withLuck(player.getLuck()).withPlayer(player);

            table.generateLootForPools(player.getRNG(), builder.build())
                    .forEach(itemStack -> spawnAsEntity(worldServer, pos, itemStack));
        }

        return true;
    }

    private static List<BlockPos> getConnectedBlocks(IBlockAccess world, BlockPos pos, List<BlockPos> visited, boolean isXAxis) {
        if (!visited.contains(pos) && world.getBlockState(pos).getBlock() instanceof BlockForgedVent) {
            visited.add(pos);

            getConnectedBlocks(world, pos.up(), visited, isXAxis);
            getConnectedBlocks(world, pos.down(), visited, isXAxis);

            if (isXAxis) {
                getConnectedBlocks(world, pos.east(), visited, isXAxis);
                getConnectedBlocks(world, pos.west(), visited, isXAxis);
            } else {
                getConnectedBlocks(world, pos.north(), visited, isXAxis);
                getConnectedBlocks(world, pos.south(), visited, isXAxis);
            }
        }

        return visited;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(BlockState state, Direction face, Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.getValue(propX) ? Direction.EAST : Direction.SOUTH, face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return BlockInteraction.attemptInteraction(world, state.getActualState(world, pos), pos, player, hand, facing, hitX, hitY, hitZ);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propRotation, propX, propBroken);
    }

    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        BlockState BlockState = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        BlockState = BlockState.withProperty(propX, Direction.Axis.X.equals(placer.getHorizontalFacing().getAxis()));

        int rotation = 0;

        if (Direction.EAST.equals(placer.getHorizontalFacing()) || Direction.SOUTH.equals(placer.getHorizontalFacing())) {
            rotation = 2;
        }

        if (facing != Direction.UP && (facing == Direction.DOWN || hitY > 0.5)) {
            rotation++;
        }

        BlockState = BlockState.withProperty(propRotation, rotation);
        
        return  BlockState;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        BlockState blockState = this.getDefaultState();
        int rotation = meta & 3;
        if (rotation < Direction.HORIZONTALS.length) {
            blockState = blockState.withProperty(propRotation, rotation);
        }
        return blockState
                .withProperty(propX, (meta >> 2 & 1) == 1)
                .withProperty(propBroken, meta >> 3 == 1);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(propRotation)
                | (state.getValue(propX) ? 1 << 2 : 0)
                | (state.getValue(propBroken) ? 1 << 3 : 0);
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        boolean isXAxis = state.getValue(propX);
        if (rot.equals(Rotation.CLOCKWISE_90) || rot.equals(Rotation.COUNTERCLOCKWISE_90)) {
            state = state.withProperty(propX, !isXAxis);
        }
        if (rot.equals(Rotation.CLOCKWISE_180)
                || (!isXAxis && rot.equals(Rotation.CLOCKWISE_90))
                || (isXAxis && rot.equals(Rotation.COUNTERCLOCKWISE_90))) {
            return state.withProperty(propRotation, state.getValue(propRotation) ^ 2);
        }

        return state.withProperty(propRotation, state.getValue(propRotation));
    }


    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(BlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(propX)) {
            return new AxisAlignedBB(0, 0, 0.4375, 1, 1, 0.5625);
        }
        return new AxisAlignedBB(0.4375, 0, 0, 0.5625, 1, 1);
    }

    @Override
    public int getLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }
}
