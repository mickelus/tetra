package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.network.TetraGuiHandler;
import se.mickelus.tetra.util.CastOptional;

public class GuiHandlerForgedContainer implements TetraGuiHandler {

    @Override
    public Object getServerGuiElement(PlayerEntity player, World world, int x, int y, int z) {
        return getContainer(player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(PlayerEntity player, World world, int x, int y, int z) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        return CastOptional.cast(tileEntity, TileEntityForgedContainer.class)
                .map(te -> new ForgedContainerScreen(getContainer(player, world, x, y, z), te))
                .orElse(null);
    }

    private ForgedContainerContainer getContainer(PlayerEntity player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        return CastOptional.cast(tileEntity, TileEntityForgedContainer.class)
                .map(te -> new ForgedContainerContainer(player.inventory, te, player))
                .orElse(null);
    }
}
