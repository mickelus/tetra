package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabililtyInteractiveOverlay {

    GuiCapabilityInteractiveOverlay gui;

    BlockPos previousPos;
    EnumFacing previousFace;
    IBlockState previousState;

    public CapabililtyInteractiveOverlay() {
        gui = new GuiCapabilityInteractiveOverlay();
    }

    @SubscribeEvent
    public void renderOverlay(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        EntityPlayer player = event.getPlayer();
        World world = player.getEntityWorld();
        BlockPos blockPos = target.getBlockPos();
        EnumFacing face = target.sideHit;

        IBlockState blockState = world.getBlockState(blockPos);
        blockState = blockState.getActualState(world, blockPos);

        if (!blockState.equals(previousState) || !blockPos.equals(previousPos) || !face.equals(previousFace)) {
            gui.update(blockState, face, player, blockPos.equals(previousPos) && face.equals(previousFace));

            previousPos = blockPos;
            previousFace = face;
            previousState = blockState;
        }

        if (blockState.getBlock() instanceof IBlockCapabilityInteractive) {
            gui.draw(player, blockPos, target.sideHit, event.getPartialTicks());
        }
    }
}
