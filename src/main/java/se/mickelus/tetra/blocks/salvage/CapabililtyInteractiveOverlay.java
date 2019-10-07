package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabililtyInteractiveOverlay {

    private GuiCapabilityInteractiveOverlay gui;

    private BlockPos previousPos;
    private Direction previousFace;
    private BlockState previousState;

    public CapabililtyInteractiveOverlay() {
        gui = new GuiCapabilityInteractiveOverlay();
    }

    @SubscribeEvent
    public void renderOverlay(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        if (target.getType() == RayTraceResult.Type.BLOCK) {
            PlayerEntity player = event.getPlayer();
            World world = player.getEntityWorld();
            BlockPos blockPos = target.getBlockPos();
            Direction face = event.getSubID()

            BlockState blockState = world.getBlockState(blockPos);
            blockState = blockState.getActualState(world, blockPos);


            if (!blockState.equals(previousState) || !blockPos.equals(previousPos) || !face.equals(previousFace)) {
                gui.update(blockState, face, player, blockPos.equals(previousPos) && face.equals(previousFace));

                previousPos = blockPos;
                previousFace = face;
                previousState = blockState;
            }

            if (blockState.getBlock() instanceof IBlockCapabilityInteractive) {
                gui.draw(player, blockPos, target, blockState.getBoundingBox(world, blockPos), event.getPartialTicks());
            }
        }
    }
}
