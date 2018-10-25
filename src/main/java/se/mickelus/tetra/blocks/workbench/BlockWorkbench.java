package se.mickelus.tetra.blocks.workbench;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchActionPacket;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Random;

public class BlockWorkbench extends TetraBlock implements ITileEntityProvider {

    static final String unlocalizedName = "workbench";

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D, 1.0D);

    public static BlockWorkbench instance;

    public BlockWorkbench() {
        super(Material.WOOD);

        setHardness(2.5f);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityWorkbench.class, unlocalizedName);

        hasItem = true;

        instance = this;

        this.setDefaultState(this.blockState.getBaseState());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWorkbench();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        player.openGui(TetraMod.instance, GuiHandlerWorkbench.GUI_WORKBENCH_ID, world, pos.getX(), pos.getY(), pos.getZ());

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

    @Override
    public void init(PacketHandler packetHandler) {
        super.init(packetHandler);

        GuiHandlerRegistry.instance.registerHandler(GuiHandlerWorkbench.GUI_WORKBENCH_ID, new GuiHandlerWorkbench());
        PacketHandler.instance.registerPacket(UpdateWorkbenchPacket.class, Side.SERVER);
        PacketHandler.instance.registerPacket(CraftWorkbenchPacket.class, Side.SERVER);
        PacketHandler.instance.registerPacket(WorkbenchActionPacket.class, Side.SERVER);
    }
}
