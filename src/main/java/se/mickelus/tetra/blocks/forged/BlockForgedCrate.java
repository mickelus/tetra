package se.mickelus.tetra.blocks.forged;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.Materials;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;


public class BlockForgedCrate extends BlockFalling implements ITetraBlock, IBlockCapabilityInteractive {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;
    public static final PropertyBool propStacked = PropertyBool.create("stacked");
    public static final PropertyInteger propIntegrity = PropertyInteger.create("integrity", 0, 3);

    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.pry, 1, EnumFacing.EAST, 6, 8, 6, 8,
                    BlockStateMatcher.ANY,
                    BlockForgedCrate::attemptBreakPry),
            new BlockInteraction(Capability.hammer, 3, EnumFacing.EAST, 1, 4, 1, 4,
                    BlockStateMatcher.ANY,
                    BlockForgedCrate::attemptBreakHammer),
            new BlockInteraction(Capability.hammer, 3, EnumFacing.EAST, 10, 13, 10, 13,
                    BlockStateMatcher.ANY,
                    BlockForgedCrate::attemptBreakHammer),
    };

    static final String unlocalizedName = "forged_crate";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedCrate instance;

    public static final ResourceLocation crateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/crate_break");

    public BlockForgedCrate() {
        super(Materials.forgedCrate);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());

        setHardness(10);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(propFacing, EnumFacing.EAST)
                .withProperty(propStacked, false)
                .withProperty(propIntegrity, 3));
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return 1;
    }

    private static boolean attemptBreakHammer(World world, BlockPos pos, IBlockState blockState, PlayerEntity player, EnumHand hand, EnumFacing facing) {
        return attemptBreak(world, pos, blockState, player, player.getHeldItem(hand), Capability.hammer, 2, 1);
    }

    private static boolean attemptBreakPry(World world, BlockPos pos, IBlockState blockState, PlayerEntity player, EnumHand hand, EnumFacing facing) {
        return attemptBreak(world, pos, blockState, player, player.getHeldItem(hand), Capability.pry, 0, 2);
    }

    private static boolean attemptBreak(World world, BlockPos pos, IBlockState blockState, PlayerEntity player, ItemStack itemStack,
            Capability capability, int min, int multiplier) {

        int integrity = blockState.getValue(propIntegrity);

        int progress = CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getCapabilityLevel(itemStack, capability))
                .map(level -> ( level - min ) * multiplier)
                .orElse(1);

        if (integrity - progress >= 0) {
            if (Capability.hammer.equals(capability)) {
                world.playSound(player, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1, 0.5f);
            } else {
                world.playSound(player, pos, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.PLAYERS, 0.7f, 2f);
            }

            world.setBlockState(pos, blockState.withProperty(propIntegrity, integrity - progress));
        } else {
            world.playEvent(player, 2001, pos, Block.getStateId(blockState));
            ItemEffectHandler.breakBlock(world, player, itemStack, pos, blockState);
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(IBlockState state, EnumFacing face, Collection<Capability> capabilities) {
            return interactions;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return BlockInteraction.attemptInteraction(world, state.getActualState(world, pos), pos, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        CastOptional.cast(world, WorldServer.class)
                .ifPresent(worldServer -> {
                    LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(crateLootTable);
                    LootContext.Builder builder = new LootContext.Builder(worldServer);

                    drops.addAll(table.generateLootForPools(worldServer.rand, builder.build()));
                });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing, propStacked, propIntegrity);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return this.getDefaultState().withProperty(propFacing, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (equals(worldIn.getBlockState(pos.down()).getBlock())) {
            return super.getActualState(state, worldIn, pos).withProperty(propStacked, true);
        }
        return super.getActualState(state, worldIn, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState blockState = this.getDefaultState();
        int facingIndex = meta & 3;
        if (facingIndex < EnumFacing.HORIZONTALS.length) {
            blockState = blockState.withProperty(propFacing, EnumFacing.HORIZONTALS[facingIndex]);
        }

        return blockState.withProperty(propIntegrity, meta >> 2);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propFacing).getHorizontalIndex()
                | (state.getValue(propIntegrity) << 2);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB aabb = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375)
                .offset(new Vec3d(state.getValue(propFacing).getDirectionVec()).scale(0.0625));

        if (getActualState(state, source, pos).getValue(propStacked)) {
            return aabb.offset(0, -0.125, 0);
        }

        return aabb;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean hasItem() {
        return true;
    }

    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        registerItem(registry, this);
    }
}
