package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import se.mickelus.tetra.items.toolbelt.ContainerToolbelt;
import se.mickelus.tetra.items.toolbelt.GuiToolbelt;

public class GuiHandlerWorkbench implements IGuiHandler {

    public static final int GUI_WORKBENCH_ID = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return getContainer(ID, player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (GUI_WORKBENCH_ID == ID) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
            if (tileEntity != null && tileEntity instanceof TileEntityWorkbench) {
                return new GuiWorkbench(getContainer(ID, player, world, x, y, z), (TileEntityWorkbench) tileEntity);
            }
        }
        return null;
    }

    private ContainerWorkbench getContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (GUI_WORKBENCH_ID == ID) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

            if (tileEntity != null && tileEntity instanceof TileEntityWorkbench) {
                return new ContainerWorkbench(player.inventory, (TileEntityWorkbench) tileEntity, player);
            }

        }
        return null;
    }
}
