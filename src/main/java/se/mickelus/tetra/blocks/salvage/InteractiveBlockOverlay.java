package se.mickelus.tetra.blocks.salvage;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (event.getTarget().getType().equals(HitResult.Type.BLOCK)) {
            BlockHitResult rayTrace = (BlockHitResult) event.getTarget();

            Level world = Minecraft.getInstance().level;
            VoxelShape shape = world.getBlockState(rayTrace.getBlockPos()).getShape(Minecraft.getInstance().level, rayTrace.getBlockPos(), CollisionContext.of(mc.player));

            BlockPos blockPos = rayTrace.getBlockPos();
            Direction face = rayTrace.getDirection();

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

                gui.draw(event.getMatrix(), event.getInfo().getPosition(), rayTrace, shape);
            }
        }
    }
}
