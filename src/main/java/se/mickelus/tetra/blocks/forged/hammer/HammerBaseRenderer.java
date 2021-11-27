package se.mickelus.tetra.blocks.forged.hammer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerTile;

@OnlyIn(Dist.CLIENT)
public class HammerBaseRenderer extends TileEntityRenderer<HammerBaseTile> {
    public static final RenderMaterial material = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(TetraMod.MOD_ID,"blocks/forged_hammer/base_sheet"));

    private final ModelRenderer unpowered;
    private final ModelRenderer powered;

    private final ModelRenderer[] modulesA;
    private final ModelRenderer[] modulesB;


    private final ModelRenderer cellAunpowered;
    private final ModelRenderer cellBunpowered;
    private final ModelRenderer cellApowered;
    private final ModelRenderer cellBpowered;

    public HammerBaseRenderer(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);

        unpowered = new ModelRenderer(128, 64, 0, 0);
        unpowered.addBox(0, 0, 0, 16, 16, 16, 0);

        powered = new ModelRenderer(128, 64, 64, 0);
        powered.addBox(0, 0, 0, 16, 16, 16, 0);

        HammerEffect[] effects = HammerEffect.values();
        modulesA = new ModelRenderer[effects.length];
        for (int i = 0; i < effects.length; i++) {
            modulesA[i] = new ModelRenderer(128, 64, i * 16, 32);
            modulesA[i].addBox(0, 0, -16, 16, 16, 0, 0.03f);
            modulesA[i].rotateAngleY = (float) -Math.PI / 2f;
        }

        modulesB = new ModelRenderer[effects.length];
        for (int i = 0; i < effects.length; i++) {
            modulesB[i] = new ModelRenderer(128, 64, i * 16, 32);
            modulesB[i].addBox(-16, 0, 0, 16, 16, 0, 0.03f);
            modulesB[i].rotateAngleY = (float) Math.PI / 2f;
        }

        cellAunpowered = new ModelRenderer(128, 64, 48, 0);
        cellAunpowered.addBox(5.5f, -19, 5.5f, 5, 3, 5, 0);
        cellAunpowered.rotateAngleX = (float) -Math.PI / 2f;
        cellApowered = new ModelRenderer(128, 64, 48, 8);
        cellApowered.addBox(5.5f, -19, 5.5f, 5, 3, 5, 0);
        cellApowered.rotateAngleX = (float) -Math.PI / 2f;

        cellBunpowered = new ModelRenderer(128, 64, 48, 0);
        cellBunpowered.addBox(5.5f, -3, -10f, 5, 3, 5, 0);
        cellBunpowered.rotateAngleX = (float) Math.PI / 2f;
        cellBpowered = new ModelRenderer(128, 64, 48, 8);
        cellBpowered.addBox(5.5f, -3, -10f, 5, 3, 5, 0);
        cellBpowered.rotateAngleX = (float) Math.PI / 2f;
    }

    @Override
    public void render(HammerBaseTile tile, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (tile.hasWorld()) {
            matrixStack.push();
            matrixStack.translate(0.5F, 0.5F, 0.5F);
            // todo: why does the model render upside down by default?
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(tile.getFacing().getHorizontalAngle()));
            matrixStack.translate(-0.5F, -0.5F, -0.5F);

            IVertexBuilder vertexBuilder = material.getBuffer(buffer, RenderType::getEntityCutout);

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

            matrixStack.pop();
        }
    }
}
