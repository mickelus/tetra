package se.mickelus.tetra.generation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.data.DataManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Optional;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ExtendedStructureRenderer extends StructureBlockRenderer {

    private final BlockEntityRendererProvider.Context dispatcher;

    public ExtendedStructureRenderer(BlockEntityRendererProvider.Context dispatcher) {
        super(dispatcher);
        this.dispatcher = dispatcher;
    }

    @Override
    public void render(StructureBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        super.render(te, partialTicks, matrixStack, buffer, combinedLight, combinedOverlay);

        BlockPos rel = te.getStructurePos();

        Optional.ofNullable(DataManager.instance.featureData.getData(new ResourceLocation(te.getStructureName())))
                .ifPresent(feature -> renderFeatureInfo(feature, matrixStack, buffer, rel.getX(), rel.getY(), rel.getZ()));
    }

    private void renderFeatureInfo(FeatureParameters feature, PoseStack matrixStack, MultiBufferSource buffer, double x, double y, double z) {
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.lines());
        RenderSystem.lineWidth(3);

        BlockPos origin = feature.origin;

        AABB aabb = new AABB(x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5,
                x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5);

        // draw center box
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.5030000000949949026D), 1, 0, 1, 1);
        DebugRenderer.renderFilledBox(aabb.inflate(0.1), 1, 1, 1, 0.6f);

        // draw outline
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.5030000000949949026D), 1, 0, 1, 1);

        Arrays.stream(feature.children).forEach(featureChild -> renderChild(featureChild, matrixStack, buffer, (float) x, (float) y, (float) z));

        Arrays.stream(feature.loot).forEach(featureLoot -> renderLoot(featureLoot, matrixStack, vertexBuilder, x, y, z));

        RenderSystem.lineWidth(1.0F);
    }

    private void renderChild(FeatureChild featureChild, PoseStack matrixStack, MultiBufferSource buffer, float x, float y, float z) {
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.lines());
        Tesselator tessellator = Tesselator.getInstance();
        Matrix4f matrix4f = matrixStack.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        BlockPos offset = featureChild.offset;

        AABB aabb = new AABB(x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5,
                x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5);

        // build outline box
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.5020000000949949026D), 1, 1, 0, 1);

        // build arrow
        // todo 1.18: this was an int (3), check that DEBUG_LINE_STRIP enum is correct
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Vec3i facing = featureChild.facing.getNormal();
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
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.inflate(0.01), 1, 1, 0, 0.5f);

        // draw position label
        drawLabel("[" + offset.toShortString() + "]", x + offset.getX(), y + offset.getY(), z + offset.getZ(), matrixStack, buffer, 15728880);
    }

    private void renderLoot(FeatureLoot featureLoot, PoseStack matrixStack, VertexConsumer vertexBuilder, double x, double y, double z) {
        BlockPos offset = featureLoot.position;

        AABB aabb = new AABB(x + offset.getX(), y + offset.getY(), z + offset.getZ(),
                x + offset.getX() + 0.2, y + offset.getY() + 0.2, z + offset.getZ() + 0.2).move(-0.1, -0.1, -0.1);

        // draw bottom blocks
        DebugRenderer.renderFilledBox(aabb, 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb, 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 0, 0), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 0, 0), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(0, 0, 1), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(0, 0, 1), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 0, 1), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 0, 1), 0, 1, 0, 1);

        // draw top blocks
        DebugRenderer.renderFilledBox(aabb.move(0, 1, 0), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(0, 1, 0), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(0, 1, 1), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(0, 1, 1), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 1, 0), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 1, 0), 0, 1, 0, 1);
        DebugRenderer.renderFilledBox(aabb.move(1, 1, 1), 0, 1, 0, 0.2f);
        LevelRenderer.renderLineBox(matrixStack, vertexBuilder, aabb.move(1, 1, 1), 0, 1, 0, 1);
    }

    protected void drawLabel(String label, float x, float y, float z, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(x, y, z);
        matrixStackIn.translate(0.5D, 0.9f, 0.5D);

        matrixStackIn.mulPose(dispatcher.getBlockEntityRenderDispatcher().camera.rotation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStackIn.last().pose();
        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int opacityBits = (int) (opacity * 255.0F) << 24;
        Font font = dispatcher.getFont();
        float xOffset = (float) (-font.width(label) / 2);
        font.draw(matrixStackIn, label, xOffset, 0, 553648127);
        font.drawInBatch(label, xOffset, 0, -1, true, matrix4f, bufferIn, false, opacityBits, packedLightIn, false);

        matrixStackIn.popPose();
    }
}
