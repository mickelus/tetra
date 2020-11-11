package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.util.TileEntityOptional;

public class InteractiveBlockOverlay {

    private Minecraft mc;

    private InteractiveBlockOverlayGui gui;

    private BlockPos previousPos;
    private Direction previousFace;
    private BlockState previousState;

    private static boolean isDirty = false;

    public InteractiveBlockOverlay() {
        gui = new InteractiveBlockOverlayGui();

        mc = Minecraft.getInstance();
    }

    public static void markDirty() {
        isDirty = true;
    }

    @SubscribeEvent
    public void renderOverlay(DrawHighlightEvent event) {
        if (event.getTarget().getType().equals(RayTraceResult.Type.BLOCK)) {
            BlockRayTraceResult rayTrace = (BlockRayTraceResult) event.getTarget();

            World world = Minecraft.getInstance().world;
            VoxelShape shape = world.getBlockState(rayTrace.getPos()).getShape(Minecraft.getInstance().world, rayTrace.getPos(), ISelectionContext.forEntity(mc.player));

            BlockPos blockPos = rayTrace.getPos();
            Direction face = rayTrace.getFace();

            BlockState blockState = world.getBlockState(blockPos);

            if (!shape.isEmpty()) {
                if (isDirty || !blockState.equals(previousState) || !blockPos.equals(previousPos) || !face.equals(previousFace)) {
                    gui.update(world, blockPos, blockState, face, Minecraft.getInstance().player,
                            blockPos.equals(previousPos) && face.equals(previousFace));

                    previousPos = blockPos;
                    previousFace = face;
                    previousState = blockState;

                    isDirty = false;
                }

                gui.draw(event.getMatrix(), event.getInfo().getProjectedView(), rayTrace, shape);
            }
        }
    }
}
