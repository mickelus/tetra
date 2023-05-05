package se.mickelus.tetra.blocks.holo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.Lherper;

public class HolosphereEntityRenderer implements BlockEntityRenderer<HolosphereBlockEntity> {
    public static final Material material = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(TetraMod.MOD_ID, "blocks/holosphere_hud"));
    private final BlockEntityRenderDispatcher dispatcher;
    private final Font font;

    public HolosphereEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockEntityRenderDispatcher();
        this.font = context.getFont();
    }

    @Override
    public void render(HolosphereBlockEntity entity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
//        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        BlockPos pos = entity.getBlockPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        Level level = entity.getLevel();

        matrixStack.pushPose();

//        matrixStack.mulPose(Vector3f.XN.rotationDegrees(90));
        matrixStack.translate(0.5, 1, 0.5);
//        drawLabel("test", matrixStack, buffer, combinedLight);
        matrixStack.popPose();

        int light = this.getLightColor(entity.getLevel(), entity.getBlockPos());

        VertexConsumer vertexBuilder = material.buffer(buffer, RenderType::entityTranslucent);
        entity.getScanResults().forEach(scan -> {
            float anim = Lherper.easeOut(Mth.clampedMap(scan.timestamp() - level.getGameTime() - partialTicks, 5, 0, 0, 1));

            if (anim < 0) {
                return;
            }

            ChunkPos renderPos = new ChunkPos(scan.chunkX() - chunkPos.x, scan.chunkZ() - chunkPos.z);
            double height = scan.height();
            int x = renderPos.x;
            float ry = 0.1f + (anim * (0.1f + (float) (0.005f * height)));
            int z = renderPos.z;
            float colorScale = Mth.clamp(0.25f + scan.temperature() / 4f, 0.1f, 0.9f);
            float opacity = 0.6f * anim;
//            if (!scan.structures().isEmpty()) {
//                renderMarker(vertexBuilder, matrixStack, level, dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, (float) (0.01f * height) + 0.2f, 0.5f + z * 0.075f, 1, 0.3f, 0.3f, 0.9f + oo);
//            }
            boolean got = !scan.structures().isEmpty();

            renderMarker(vertexBuilder, matrixStack, level, dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, ry, 0.5f + z * 0.075f, 1, got ? 0.2f : 1, got ? 0.2f : 1, opacity);
            for (int k = 0; k < 3; k++) {
                float anim2 = Lherper.easeOut(Mth.clampedMap(scan.timestamp() + k * 2 + 5 - level.getGameTime() - partialTicks, 5, 0, 0, 1));
                float opacity2 = (0.5f - k * 0.1f) * anim2;
                float ry2 = 0.1f + (anim2 * (0.1f + (float) (0.005f * (height - k * 15))));
                renderMarker(vertexBuilder, matrixStack, level, dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, ry2, 0.5f + z * 0.075f, 1, got ? 0.2f : 1, got ? 0.2f : 1, opacity2);
            }
//            renderMarker(vertexBuilder, matrixStack, level, dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.5f, 0.5f + z * 0.075f, 1, 1, 1, 0.2f);
        });
//        for (int i = -5; i <= 5; i++) {
//            for (int j = -5; j <= 5; j++) {
//                int x = i + j;
//                int z = i - j;
//                int height = entity.getLevel().getChunk(x, z).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
//                renderMarker(vertexBuilder, matrixStack, entity.getLevel(), dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.01f * height, 0.5f + z * 0.075f, 1, 1, 1, 0.9f);
//                for (int k = height / 10; k > 0; k--) {
//                    renderMarker(vertexBuilder, matrixStack, entity.getLevel(), dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.1f * k, 0.5f + z * 0.075f, 1, 1, 1, k * 0.05f);
//                }
//                renderMarker(vertexBuilder, matrixStack, entity.getLevel(), dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.5f, 0.5f + z * 0.075f, 1, 1, 1, 0.2f);
//            }
//        }

//        GlStateManager.lineWidth(3);

//        AABB aabb = new AABB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
//                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        AABB aabb = new AABB(0.5, 0.5, 0.5,
                0.5, 0.5, 0.5);

        // draw center box
//        VertexConsumer vertexBuilder2 = buffer.getBuffer(RenderType.lines());
//        LevelRenderer.renderLineBox(matrixStack, vertexBuilder2, aabb.inflate(0.5030000000949949026D), 1, 0, 1, 1);
//        LevelRenderer.renderLineBox(p_112585_, vertexconsumer, d4, d5, d6, d7, d8, d9, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
//        DebugRenderer.renderLineBox(aabb.grow(0.1), 1, 1, 1, 0.6f);
//
//        // draw outline
//        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.grow(0.5030000000949949026D), 1, 0, 1, 1);
//
//        Arrays.stream(feature.children).forEach(featureChild -> renderChild(featureChild, matrixStack, buffer, (float) x, (float) y, (float) z));
//
//        Arrays.stream(feature.loot).forEach(featureLoot -> renderLoot(featureLoot, matrixStack, vertexBuilder, x, y, z));
//
//        GlStateManager.lineWidth(1.0F);
    }

    public void renderMarker(VertexConsumer consumer, PoseStack poseStack, Level level, Camera camera, TextureAtlasSprite sprite,
            float partialTicks, int light, float x, float y, float z, float r, float g, float b, float a) {
        drawQuad(consumer, poseStack, level, camera, sprite, light, 1, 0, 0, x, y, z, r, g, b, a);
    }

    private void drawQuad(VertexConsumer consumer, PoseStack poseStack, Level level, Camera camera, TextureAtlasSprite sprite,
            int light, int size, int u, int v, float x, float y, float z, float r, float g, float b, float a) {
        float quadSize = size / 16f;

        Quaternion quaternion = camera.rotation();
        PoseStack.Pose pose = poseStack.last();
        Matrix3f normal = pose.normal();
        Matrix4f matrix4f = pose.pose();

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.transform(quaternion);
        Vector3f[] matrix = new Vector3f[]{
                new Vector3f(-0.5F, -0.5F, 0.0F),
                new Vector3f(-0.5F, 0.5F, 0.0F),
                new Vector3f(0.5F, 0.5F, 0.0F),
                new Vector3f(0.5F, -0.5F, 0.0F)
        };

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = matrix[i];
            vector3f.transform(quaternion);
            vector3f.mul(quadSize);
//            vector3f.add(x - (float) cameraPos.x(), y - (float) cameraPos.y(), z - (float) cameraPos.z());
            vector3f.add(x, y, z);
        }

        consumer.vertex(matrix4f, matrix[0].x(), matrix[0].y(), matrix[0].z()).color(r, g, b, a).uv(sprite.getU(u), sprite.getV(v))
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix4f, matrix[1].x(), matrix[1].y(), matrix[1].z()).color(r, g, b, a).uv(sprite.getU(u + size), sprite.getV(v))
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix4f, matrix[2].x(), matrix[2].y(), matrix[2].z()).color(r, g, b, a).uv(sprite.getU(u + size), sprite.getV(v + size))
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix4f, matrix[3].x(), matrix[3].y(), matrix[3].z()).color(r, g, b, a).uv(sprite.getU(u), sprite.getV(v + size))
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    private int getLightColor(Level level, BlockPos pos) {
        return 15728880;//level.hasChunkAt(pos) ? LevelRenderer.getLightColor(level, pos) : 0;
    }

    private void drawLabel(String label, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.scale(-0.0125f, -0.0125f, 0.0125f);
        Matrix4f matrix4f = matrixStack.last().pose();
        float x = -font.width(label) / 2f;
        font.drawInBatch(label, x + 1, 0, 0, false, matrix4f, buffer, false, 0, packedLight, false);
        font.drawInBatch(label, x - 1, 0, 0, false, matrix4f, buffer, false, 0, packedLight, false);
        font.drawInBatch(label, x, -1, 0, false, matrix4f, buffer, false, 0, packedLight, false);
        font.drawInBatch(label, x, 1, 0, false, matrix4f, buffer, false, 0, packedLight, false);

        matrixStack.translate(0, 0, -0.0125f);
        font.drawInBatch(label, x, 0, -1, false, matrix4f, buffer, false, 0, packedLight, false);
    }
}
