package se.mickelus.tetra.blocks.forged.hammer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
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
public class HammerBaseRenderer implements BlockEntityRenderer<HammerBaseTile> {
    public static final Material material = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(TetraMod.MOD_ID,"blocks/forged_hammer/base_sheet"));
    public static ModelLayerLocation layer = new ModelLayerLocation(new ResourceLocation(TetraMod.MOD_ID, HammerBaseBlock.unlocalizedName), "main");

    private /*final*/ ModelPart unpowered;
    private /*final*/ ModelPart powered;

    private /*final*/ ModelPart[] modulesA;
    private /*final*/ ModelPart[] modulesB;


    private /*final*/ ModelPart cellAunpowered;
    private /*final*/ ModelPart cellBunpowered;
    private /*final*/ ModelPart cellApowered;
    private /*final*/ ModelPart cellBpowered;

    public HammerBaseRenderer(BlockEntityRendererProvider.Context context) {

    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
//        unpowered = new ModelPart(128, 64, 0, 0);
//        unpowered.addBox(0, 0, 0, 16, 16, 16, 0);
//
//        powered = new ModelPart(128, 64, 64, 0);
//        powered.addBox(0, 0, 0, 16, 16, 16, 0);
//
//        HammerEffect[] effects = HammerEffect.values();
//        modulesA = new ModelPart[effects.length];
//        for (int i = 0; i < effects.length; i++) {
//            modulesA[i] = new ModelPart(128, 64, i * 16, 32);
//            modulesA[i].addBox(0, 0, -16, 16, 16, 0, 0.03f);
//            modulesA[i].yRot = (float) -Math.PI / 2f;
//        }
//
//        modulesB = new ModelPart[effects.length];
//        for (int i = 0; i < effects.length; i++) {
//            modulesB[i] = new ModelPart(128, 64, i * 16, 32);
//            modulesB[i].addBox(-16, 0, 0, 16, 16, 0, 0.03f);
//            modulesB[i].yRot = (float) Math.PI / 2f;
//        }
//
//        cellAunpowered = new ModelPart(128, 64, 48, 0);
//        cellAunpowered.addBox(5.5f, -19, 5.5f, 5, 3, 5, 0);
//        cellAunpowered.xRot = (float) -Math.PI / 2f;
//        cellApowered = new ModelPart(128, 64, 48, 8);
//        cellApowered.addBox(5.5f, -19, 5.5f, 5, 3, 5, 0);
//        cellApowered.xRot = (float) -Math.PI / 2f;
//
//        cellBunpowered = new ModelPart(128, 64, 48, 0);
//        cellBunpowered.addBox(5.5f, -3, -10f, 5, 3, 5, 0);
//        cellBunpowered.xRot = (float) Math.PI / 2f;
//        cellBpowered = new ModelPart(128, 64, 48, 8);
//        cellBpowered.addBox(5.5f, -3, -10f, 5, 3, 5, 0);
//        cellBpowered.xRot = (float) Math.PI / 2f;

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(HammerBaseTile tile, float v, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (tile.hasLevel()) {
            matrixStack.pushPose();
            matrixStack.translate(0.5F, 0.5F, 0.5F);
            // todo: why does the model render upside down by default?
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(tile.getFacing().toYRot()));
            matrixStack.translate(-0.5F, -0.5F, -0.5F);

            VertexConsumer vertexBuilder = material.buffer(buffer, RenderType::entityCutout);

            if (tile.isFueled()) {
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
