package se.mickelus.tetra.blocks.workbench;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.hammer.BlockHammerHead;
import se.mickelus.tetra.blocks.workbench.action.ConfigActionImpl;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchActionPacket;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockWorkbench extends TetraBlock implements ITileEntityProvider {

    public static final PropertyEnum<Variant> propVariant = PropertyEnum.create("variant", Variant.class);

    static final String unlocalizedName = "workbench";

    public static final AxisAlignedBB forgedAABB = new AxisAlignedBB(0.125, 0, 0, 0.875, 1, 1);

    public static BlockWorkbench instance;

    public BlockWorkbench() {
        super(Material.WOOD);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityWorkbench.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraCreativeTabs.getInstance());

        hasItem = true;

        instance = this;

        this.setDefaultState(this.blockState.getBaseState().withProperty(propVariant, Variant.wood));
    }



    public static EnumActionResult upgradeWorkbench(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return EnumActionResult.FAIL;
        }

        if (world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)) {

            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.5F);

            if (!world.isRemote) {
                world.setBlockState(pos, instance.getDefaultState());

                // todo: add proper criteria ?
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemStack);
            }
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    /**
     * Special item regististration to handle multiple variants registered in the creative menu.
     * @param registry Item registry
     */
    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        Item item = new ItemBlock(this) {
            @Override
            public int getMetadata(int damage) {
                return damage;
            }
        };
        item.setRegistryName(getRegistryName());
        registry.register(item);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            for (Variant variant : Variant.values()) {
                ModelLoader.setCustomModelResourceLocation(item, variant.ordinal(), new ModelResourceLocation(getRegistryName(), "variant=" + variant.toString()));
            }
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs creativeTabs, NonNullList<ItemStack> items) {
        if (TetraCreativeTabs.getInstance().equals(creativeTabs)) {
            items.add(new ItemStack(this, 1, Variant.wood.ordinal()));
            if (ConfigHandler.generate_features) {
                items.add(new ItemStack(this, 1, Variant.forged.ordinal()));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (stack.getItemDamage() == Variant.forged.ordinal()) {
            tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWorkbench();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        player.openGui(TetraMod.instance, GuiHandlerWorkbench.workbenchId, world, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (ConfigHandler.workbenchDropTable) {
            return Blocks.CRAFTING_TABLE.getItemDropped(Blocks.CRAFTING_TABLE.getDefaultState(), rand, fortune);
        } else {
            return super.getItemDropped(state, rand, fortune);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);

        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(world, pos, (IInventory) tileentity);
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return Blocks.CRAFTING_TABLE.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public Collection<Capability> getCapabilities(World world, BlockPos pos, IBlockState blockState) {
        IBlockState accessoryBlockState = world.getBlockState(pos.offset(EnumFacing.UP));
        if (accessoryBlockState.getBlock() instanceof ITetraBlock) {
            ITetraBlock block = (ITetraBlock) accessoryBlockState.getBlock();
            return block.getCapabilities(world, pos.offset(EnumFacing.UP), accessoryBlockState);
        }
        return Collections.emptyList();
    }

    @Override
    public int getCapabilityLevel(World world, BlockPos pos, IBlockState blockState, Capability capability) {
        IBlockState accessoryBlockState = world.getBlockState(pos.offset(EnumFacing.UP));
        if (accessoryBlockState.getBlock() instanceof ITetraBlock) {
            ITetraBlock block = (ITetraBlock) accessoryBlockState.getBlock();
            return block.getCapabilityLevel(world, pos.offset(EnumFacing.UP), accessoryBlockState, capability);
        }
        return -1;
    }

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, EntityPlayer player, boolean consumeResources) {
        BlockPos topPos = pos.offset(EnumFacing.UP);
        if (world.getBlockState(topPos).getBlock() instanceof BlockHammerHead) {
            BlockHammerHead hammer = (BlockHammerHead) world.getBlockState(topPos).getBlock();
            return hammer.onCraftConsumeCapability(world, topPos, world.getBlockState(topPos), targetStack, player, consumeResources);
        }
        return targetStack;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, EntityPlayer player, boolean consumeResources) {
        BlockPos topPos = pos.offset(EnumFacing.UP);
        if (world.getBlockState(topPos).getBlock() instanceof BlockHammerHead) {
            BlockHammerHead hammer = (BlockHammerHead) world.getBlockState(topPos).getBlock();
            return hammer.onActionConsumeCapability(world, topPos, world.getBlockState(topPos), targetStack, player, consumeResources);
        }
        return targetStack;
    }

    @Override
    public void init(PacketHandler packetHandler) {
        super.init(packetHandler);

        TileEntityWorkbench.initConfigActions(DataHandler.instance.getData("actions", ConfigActionImpl[].class));

        GuiHandlerRegistry.instance.registerHandler(GuiHandlerWorkbench.workbenchId, new GuiHandlerWorkbench());
        PacketHandler.instance.registerPacket(UpdateWorkbenchPacket.class, Side.SERVER);
        PacketHandler.instance.registerPacket(CraftWorkbenchPacket.class, Side.SERVER);
        PacketHandler.instance.registerPacket(WorkbenchActionPacket.class, Side.SERVER);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propVariant);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta < Variant.values().length) {
            return getDefaultState().withProperty(propVariant, Variant.values()[meta]);
        }
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propVariant).ordinal();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return state.getValue(propVariant).equals(Variant.wood);
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return isFullCube(state);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return state.getValue(propVariant).equals(Variant.wood) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state.getValue(propVariant).equals(Variant.wood);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(propVariant).equals(Variant.wood) ? FULL_BLOCK_AABB : forgedAABB;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(propVariant).equals(Variant.wood) ? lightOpacity : 0;
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(propVariant).getMaterial();
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        return state.getValue(propVariant).getHardness();
    }

    public static enum Variant implements IStringSerializable {
        wood(Material.WOOD, 2.5f),
        forged(Material.ANVIL, -1);

        private final Material material;
        private final float hardness;

        Variant( Material material, float hardness) {
            this.material = material;
            this.hardness = hardness;
        }

        Material getMaterial() {
            return material;
        }

        public float getHardness() {
            return hardness;
        }

        @Override
        public String getName() {
            return toString();
        }
    }
}
