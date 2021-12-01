package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ScannerDebugRenderer {
    private final ScannerOverlayGui overlayGui;

    public ScannerDebugRenderer(ScannerOverlayGui overlayGui) {
        this.overlayGui = overlayGui;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderWorld(RenderWorldLastEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;

        if (player != null && player.isCreative()) {
            MatrixStack matrixStack = event.getMatrixStack();
            IVertexBuilder vertexBuilder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
            Vector3d eyePos = Minecraft.getInstance().player.getEyePosition(event.getPartialTicks());

            GlStateManager._lineWidth(3);
            if (overlayGui.upHighlight != null) drawDebugBox(overlayGui.upHighlight, eyePos, matrixStack, vertexBuilder, 1, 0, 0, 0.5f);
            if (overlayGui.midHighlight != null) drawDebugBox(overlayGui.midHighlight, eyePos, matrixStack, vertexBuilder, 0, 1, 0, 0.5f);
            if (overlayGui.downHighlight != null) drawDebugBox(overlayGui.downHighlight, eyePos, matrixStack, vertexBuilder, 0, 0, 1, 0.5f);
            GlStateManager._lineWidth(1.0F);
        }
    }

    private void drawDebugBox(BlockPos blockPos, Vector3d eyePos, MatrixStack matrixStack, IVertexBuilder vertexBuilder, float red, float green, float blue, float alpha) {
        Vector3d pos = Vector3d.atLowerCornerOf(blockPos).subtract(eyePos);
        AxisAlignedBB aabb = new AxisAlignedBB(pos, pos.add(1, 1, 1));

        // draw center box
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.0030000000949949026D), red, green, blue, alpha);
    }
}
