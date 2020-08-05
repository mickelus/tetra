package se.mickelus.tetra.generation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.tileentity.StructureTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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

        BlockPos rel = te.getPosition();

        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.getLines());

        Optional.ofNullable(DataManager.featureData.getData(new ResourceLocation(te.getName())))
                .ifPresent(feature -> renderFeatureInfo(feature, matrixStack, vertexBuilder, rel.getX(), rel.getY(), rel.getZ()));
    }

    private void renderFeatureInfo(FeatureParameters feature, MatrixStack matrixStack, IVertexBuilder vertexBuilder, double x, double y, double z) {
        GlStateManager.lineWidth(3);

        BlockPos origin = feature.origin;

        AxisAlignedBB aabb = new AxisAlignedBB(x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5,
                x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5);

        // draw center box
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.grow(0.5030000000949949026D), 1, 0, 1, 1);
        DebugRenderer.renderBox(aabb.grow(0.1), 1, 1, 1, 0.6f);

        // draw outline
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.grow(0.5030000000949949026D), 1, 0, 1, 1);

        Arrays.stream(feature.children).forEach(featureChild -> renderChild(featureChild, matrixStack, vertexBuilder, x, y, z));

        Arrays.stream(feature.loot).forEach(featureLoot -> renderLoot(featureLoot, matrixStack, vertexBuilder, x, y, z));

        GlStateManager.lineWidth(1.0F);
    }

    private void renderChild(FeatureChild featureChild, MatrixStack matrixStack, IVertexBuilder vertexBuilder, double x, double y, double z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        BlockPos offset = featureChild.offset;

        AxisAlignedBB aabb = new AxisAlignedBB(x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5,
                x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5);

        // build outline box
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.grow(0.5020000000949949026D), 1, 1, 0, 1);

        // build arrow
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        Vector3i facing = featureChild.facing.getDirectionVec();
        bufferBuilder.pos(x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5).color(0.0F, 0.0F, 0.0F, 0.0F).endVertex();
        bufferBuilder.pos(
                x + offset.getX() + 0.5 + 0.3 * facing.getX(),
                y + offset.getY() + 0.5 + 0.3 * facing.getY(),
                z + offset.getZ() + 0.5 + 0.3 * facing.getZ())
                .color(1, 1, 1, 1.0F).endVertex();

        tessellator.draw();

        // draw middle block
        DebugRenderer.renderBox(aabb.grow(0.1), 0, 0, 0, 0.8f);
    }

    private void renderLoot(FeatureLoot featureLoot, MatrixStack matrixStack, IVertexBuilder vertexBuilder, double x, double y, double z) {
        BlockPos offset = featureLoot.position;

        AxisAlignedBB aabb = new AxisAlignedBB(x + offset.getX(), y + offset.getY(), z + offset.getZ(),
                x + offset.getX() + 0.2, y + offset.getY() + 0.2, z + offset.getZ() + 0.2).offset(-0.1, -0.1, -0.1);

        // draw bottom blocks
        DebugRenderer.renderBox(aabb, 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb, 0, 1, 0, 1);
        DebugRenderer.renderBox(aabb.offset(1, 0, 0), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(1, 0, 0), 0, 1, 0, 1);
        DebugRenderer.renderBox(aabb.offset(0, 0, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(0, 0, 1), 0, 1, 0, 1);
        DebugRenderer.renderBox(aabb.offset(1, 0, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(1, 0, 1), 0, 1, 0, 1);

        // draw top blocks
        DebugRenderer.renderBox(aabb.offset(0, 1, 0), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(0, 1, 0), 0, 1, 0, 1);
        DebugRenderer.renderBox(aabb.offset(0, 1, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(0, 1, 1), 0, 1, 0, 1);
        DebugRenderer.renderBox(aabb.offset(1, 1, 0), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(1, 1, 0), 0, 1, 0, 1);
        DebugRenderer.renderBox(aabb.offset(1, 1, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.offset(1, 1, 1), 0, 1, 0, 1);
    }
}
