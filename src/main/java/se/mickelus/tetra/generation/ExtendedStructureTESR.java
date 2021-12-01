package se.mickelus.tetra.generation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.tileentity.StructureTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.data.DataManager;

import java.util.Arrays;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ExtendedStructureTESR extends StructureTileEntityRenderer {

    public ExtendedStructureTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(StructureBlockTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        super.render(te, partialTicks, matrixStack, buffer, combinedLight, combinedOverlay);

        BlockPos rel = te.getStructurePos();

        Optional.ofNullable(DataManager.featureData.getData(new ResourceLocation(te.getStructureName())))
                .ifPresent(feature -> renderFeatureInfo(feature, matrixStack, buffer, rel.getX(), rel.getY(), rel.getZ()));
    }

    private void renderFeatureInfo(FeatureParameters feature, MatrixStack matrixStack, IRenderTypeBuffer buffer, double x, double y, double z) {
        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
        GlStateManager._lineWidth(3);

        BlockPos origin = feature.origin;

        AxisAlignedBB aabb = new AxisAlignedBB(x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5,
                x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5);

        // draw center box
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.5030000000949949026D), 1, 0, 1, 1);
        DebugRenderer.renderFilledBox(aabb.inflate(0.1), 1, 1, 1, 0.6f);

        // draw outline
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.5030000000949949026D), 1, 0, 1, 1);

        Arrays.stream(feature.children).forEach(featureChild -> renderChild(featureChild, matrixStack, buffer, (float) x, (float) y, (float) z));

        Arrays.stream(feature.loot).forEach(featureLoot -> renderLoot(featureLoot, matrixStack, vertexBuilder, x, y, z));

        GlStateManager._lineWidth(1.0F);
    }

    private void renderChild(FeatureChild featureChild, MatrixStack matrixStack, IRenderTypeBuffer buffer, float x, float y, float z) {
        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f matrix4f = matrixStack.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        BlockPos offset = featureChild.offset;

        AxisAlignedBB aabb = new AxisAlignedBB(x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5,
                x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5);

        // build outline box
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.5020000000949949026D), 1, 1, 0, 1);

        // build arrow
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        Vector3i facing = featureChild.facing.getNormal();
        vertexBuilder.vertex(matrix4f, x + offset.getX() + 0.5f, y + offset.getY() + 0.5f, z + offset.getZ() + 0.5f)
                .color(0.0F, 0.0F, 0.0F, 0.0F)
                .endVertex();

        vertexBuilder.vertex(matrix4f,
                x + offset.getX() + 0.5f + 0.3f * facing.getX(),
                y + offset.getY() + 0.5f + 0.3f * facing.getY(),
                z + offset.getZ() + 0.5f + 0.3f * facing.getZ())
                .color(1, 1, 1, 1.0F)
                .endVertex();

        tessellator.end();

        // draw middle block
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.01), 1, 1, 0, 0.5f);

        // draw position label
        drawLabel("[" + offset.toShortString() + "]", x + offset.getX(), y + offset.getY(), z + offset.getZ(), matrixStack, buffer, 15728880);
    }

    private void renderLoot(FeatureLoot featureLoot, MatrixStack matrixStack, IVertexBuilder vertexBuilder, double x, double y, double z) {
        BlockPos offset = featureLoot.position;

        AxisAlignedBB aabb = new AxisAlignedBB(x + offset.getX(), y + offset.getY(), z + offset.getZ(),
                x + offset.getX() + 0.2, y + offset.getY() + 0.2, z + offset.getZ() + 0.2).move(-0.1, -0.1, -0.1);

        // draw bottom blocks
        DebugRenderer.renderFilledBox(aabb, 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb, 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 0, 0), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 0, 0), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(0, 0, 1), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(0, 0, 1), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 0, 1), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 0, 1), 0, 1, 0, 1);

        // draw top blocks
        DebugRenderer.renderFilledBox(aabb.move(0, 1, 0), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(0, 1, 0), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(0, 1, 1), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(0, 1, 1), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 1, 0), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 1, 0), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 1, 1), 0, 1, 0, 0.2f);
        WorldRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 1, 1), 0, 1, 0, 1);
    }

    protected void drawLabel(String label, float x, float y, float z, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(x, y, z);
        matrixStackIn.translate(0.5D, 0.9f, 0.5D);

        matrixStackIn.mulPose(renderer.camera.rotation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStackIn.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int) (f1 * 255.0F) << 24;
        FontRenderer fontrenderer = renderer.font;
        float f2 = (float) (-fontrenderer.width(label) / 2);
        fontrenderer.draw(matrixStackIn, label, f2, 0, 553648127);
        fontrenderer.drawInBatch(label, f2, 0, -1, true, matrix4f, bufferIn, false, j, packedLightIn, false);

        matrixStackIn.popPose();
    }
}
