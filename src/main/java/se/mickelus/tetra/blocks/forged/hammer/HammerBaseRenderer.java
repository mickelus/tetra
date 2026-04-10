package se.mickelus.tetra.blocks.forged.hammer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class HammerBaseRenderer implements BlockEntityRenderer<HammerBaseBlockEntity> {
    public static final Material material = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "block/forged_hammer/base_sheet"));
    public static ModelLayerLocation layer = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, HammerBaseBlock.identifier), "main");

    private final ModelPart unpowered;
    private final ModelPart powered;

    private final ModelPart[] modulesA;
    private final ModelPart[] modulesB;


    private final ModelPart cellAunpowered;
    private final ModelPart cellBunpowered;
    private final ModelPart cellApowered;
    private final ModelPart cellBpowered;

    public HammerBaseRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(layer);

        unpowered = modelpart.getChild("unpowered");
        powered = modelpart.getChild("powered");
        HammerEffect[] effects = HammerEffect.values();
        modulesA = new ModelPart[effects.length];
        modulesB = new ModelPart[effects.length];
        for (int i = 0; i < effects.length; i++) {
            modulesA[i] = modelpart.getChild("moduleA" + i);
            modulesB[i] = modelpart.getChild("moduleB" + i);
        }
        cellAunpowered = modelpart.getChild("cellAunpowered");
        cellBunpowered = modelpart.getChild("cellBunpowered");
        cellApowered = modelpart.getChild("cellApowered");
        cellBpowered = modelpart.getChild("cellBpowered");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("unpowered", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(0, 0, 0, 16, 16, 16), PartPose.ZERO);

        parts.addOrReplaceChild("powered", CubeListBuilder.create()
                .texOffs(64, 0)
                .addBox(0, 0, 0, 16, 16, 16), PartPose.ZERO);

        HammerEffect[] effects = HammerEffect.values();
        for (int i = 0; i < effects.length; i++) {
            parts.addOrReplaceChild("moduleA" + i, CubeListBuilder.create()
                            .texOffs(i * 16, 32)
                            .addBox(0, 0, -16, 16, 16, 0, new CubeDeformation(0.03f)),
                    PartPose.offsetAndRotation(0, 0, 0, 0, -Mth.PI / 2f, 0));
            parts.addOrReplaceChild("moduleB" + i, CubeListBuilder.create()
                            .texOffs(i * 16, 32)
                            .addBox(-16, 0, 0, 16, 16, 0, new CubeDeformation(0.03f)),
                    PartPose.offsetAndRotation(0, 0, 0, 0, Mth.PI / 2f, 0));
        }


        parts.addOrReplaceChild("cellAunpowered", CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(5.5f, -19, 5.5f, 5, 3, 5),
                PartPose.offsetAndRotation(0, 0, 0, -Mth.PI / 2f, 0, 0));
        parts.addOrReplaceChild("cellApowered", CubeListBuilder.create()
                        .texOffs(48, 8)
                        .addBox(5.5f, -19, 5.5f, 5, 3, 5),
                PartPose.offsetAndRotation(0, 0, 0, -Mth.PI / 2f, 0, 0));


        parts.addOrReplaceChild("cellBunpowered", CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(5.5f, -3, -10.5f, 5, 3, 5),
                PartPose.offsetAndRotation(0, 0, 0, Mth.PI / 2f, 0, 0));
        parts.addOrReplaceChild("cellBpowered", CubeListBuilder.create()
                        .texOffs(48, 8)
                        .addBox(5.5f, -3, -10.5f, 5, 3, 5),
                PartPose.offsetAndRotation(0, 0, 0, Mth.PI / 2f, 0, 0));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(HammerBaseBlockEntity tile, float v, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (tile.hasLevel()) {
            matrixStack.pushPose();
            matrixStack.translate(0.5F, 0.5F, 0.5F);
            // todo: why does the model render upside down by default?
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
            matrixStack.mulPose(Axis.YP.rotationDegrees(tile.getFacing().toYRot()));
            matrixStack.translate(-0.5F, -0.5F, -0.5F);

            VertexConsumer vertexBuilder = material.buffer(buffer, RenderType::entityCutout);

            if (tile.isFunctional()) {
                powered.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            } else {
                unpowered.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            }

            if (tile.hasCellInSlot(0)) {
                if (tile.getCellFuel(0) > 0) {
                    cellApowered.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
                } else {
                    cellAunpowered.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
                }
            }

            if (tile.hasCellInSlot(1)) {
                if (tile.getCellFuel(1) > 0) {
                    cellBpowered.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
                } else {
                    cellBunpowered.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
                }
            }

            if (tile.getEffect(true) != null) {
                modulesA[tile.getEffect(true).ordinal()].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            }

            if (tile.getEffect(false) != null) {
                modulesB[tile.getEffect(false).ordinal()].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            }

            matrixStack.popPose();
        }
    }
}
