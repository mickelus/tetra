package se.mickelus.tetra.blocks.forged.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ForgedContainerRenderer implements BlockEntityRenderer<ForgedContainerTile> {
    public static final Material material = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(TetraMod.MOD_ID,"blocks/forged_container/forged_container"));
    public static ModelLayerLocation layer = new ModelLayerLocation(new ResourceLocation(TetraMod.MOD_ID, ForgedContainerBlock.unlocalizedName), "main");


    public ModelPart lid;
    public ModelPart base;

    public ModelPart locks[];

    private static final float openDuration = 300;

    public ForgedContainerRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.BANNER);
        this.lid = modelpart.getChild("lid");

        locks = new ModelPart[4];
        for (int i = 0; i < 4; i++) {
            this.locks[i] = modelpart.getChild("lid" + i);
        }
        this.base = modelpart.getChild("base");
    }
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("lid", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(0, -3, -14, 30, 3, 14), PartPose.ZERO);
//        lid.x = 1;
//        lid.y = 7;
//        lid.z = 15;

        for (int i = 0; i < 4; i++) {
            parts.addOrReplaceChild("locks" + i, CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-2 + i * 6, -1, -14.03f, 2, 3, 1), PartPose.ZERO);
//            locks[i].x = 8;
//            locks[i].y = 7;
//            locks[i].z = 15;
        }

        parts.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 17)
                .addBox(0, 1, 0, 30, 9, 14), PartPose.ZERO);
//        base.x = 1;
//        base.y = 6;
//        base.z = 1;

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(ForgedContainerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
            int combinedLight, int combinedOverlay) {
        if (tile.isFlipped()) {
            return;
        }

        if (tile.hasLevel()) {
            matrixStack.pushPose();
            matrixStack.translate(0.5F, 0.5F, 0.5F);
            // todo: why does the model render upside down by default?
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(tile.getFacing().toYRot()));
            matrixStack.translate(-0.5F, -0.5F, -0.5F);

            VertexConsumer vertexBuilder = material.buffer(renderTypeBuffer, RenderType::entitySolid);

            renderLid(tile, partialTicks, matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            renderLocks(tile, partialTicks, matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            base.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);

            matrixStack.popPose();
        }
    }

    private void renderLid(ForgedContainerTile tile, float partialTicks, PoseStack matrixStack, VertexConsumer vertexBuilder,
            int combinedLight, int combinedOverlay) {
        if (tile.isOpen()) {
            float progress = Math.min(1, (System.currentTimeMillis() - tile.openTime) / openDuration);
            lid.yRot = (progress * 0.1f * ((float) Math.PI / 2F));

            matrixStack.translate(0,0, 0.3f * progress);
            lid.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            matrixStack.translate(0,0, -0.3f * progress);

        } else {
            lid.yRot = 0;
            lid.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
        }
    }

    private void renderLocks(ForgedContainerTile tile, float partialTicks, PoseStack matrixStack, VertexConsumer vertexBuilder,
            int combinedLight, int combinedOverlay) {
        Boolean[] locked = tile.isLocked();
        for (int i = 0; i < locks.length; i++) {
            if (locked[i]) {
                locks[i].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            }
        }
    }
}
