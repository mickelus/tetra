package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (event.getTarget().getType().equals(RayTraceResult.Type.BLOCK)) {
            BlockRayTraceResult rayTrace = (BlockRayTraceResult) event.getTarget();

            World world = Minecraft.getInstance().world;
            VoxelShape shape = world.getBlockState(rayTrace.getPos()).getShape(Minecraft.getInstance().world, rayTrace.getPos());

            BlockPos blockPos = rayTrace.getPos();
            Direction face = rayTrace.getFace();

            BlockState blockState = world.getBlockState(blockPos);

            if (!shape.isEmpty()) {
                if (!blockState.equals(previousState) || !blockPos.equals(previousPos) || !face.equals(previousFace)) {
                    gui.update(blockState, face, Minecraft.getInstance().player, blockPos.equals(previousPos) && face.equals(previousFace));

                    previousPos = blockPos;
                    previousFace = face;
                    previousState = blockState;
                }

                gui.draw(Minecraft.getInstance().player, rayTrace, shape, event.getPartialTicks());
            }
        }
    }
}
