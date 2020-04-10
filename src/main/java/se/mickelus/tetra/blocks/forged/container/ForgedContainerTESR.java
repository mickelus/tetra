package se.mickelus.tetra.blocks.forged.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.BellTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;

// todo 1.15: ripped out
@OnlyIn(Dist.CLIENT)
public class ForgedContainerTESR extends TileEntityRenderer<ForgedContainerTile> {
    public static final Material material = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(TetraMod.MOD_ID,"blocks/forged_container/forged_container"));

    public ModelRenderer lid;
    public ModelRenderer base;

    public ModelRenderer locks[];

    private static final float openDuration = 300;

    public ForgedContainerTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        lid = new ModelRenderer(128, 64, 0, 0);
        lid.addBox(0, -3, -14, 30, 3, 14, 0);
        lid.rotationPointX = 1;
        lid.rotationPointY = 7;
        lid.rotationPointZ = 15;

        locks = new ModelRenderer[4];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ModelRenderer(128, 64, 0, 0);
            locks[i].addBox(-2 + i * 6, -1, -14.03f, 2, 3, 1, 0);
            locks[i].rotationPointX = 8;
            locks[i].rotationPointY = 7;
            locks[i].rotationPointZ = 15;
        }

        base = new ModelRenderer(128, 64, 0, 17);
        base.addBox(0, 1, 0, 30, 9, 14, 0);
        base.rotationPointX = 1;
        base.rotationPointY = 6;
        base.rotationPointZ = 1;
    }

    @Override
    public void render(ForgedContainerTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
            int combinedLight, int combinedOverlay) {
        if (tile.isFlipped()) {
            return;
        }

        if (tile.hasWorld()) {
            matrixStack.push();
            matrixStack.translate(0.5F, 0.5F, 0.5F);
            // todo: why does the model render upside down by default?
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(tile.getFacing().getHorizontalAngle()));
            matrixStack.translate(-0.5F, -0.5F, -0.5F);

            IVertexBuilder vertexBuilder = material.getBuffer(renderTypeBuffer, RenderType::getEntitySolid);

            renderLid(tile, partialTicks, matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            renderLocks(tile, partialTicks, matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            base.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);

            matrixStack.pop();
        }
    }

    private void renderLid(ForgedContainerTile tile, float partialTicks, MatrixStack matrixStack, IVertexBuilder vertexBuilder,
            int combinedLight, int combinedOverlay) {
        if (tile.isOpen()) {
            float progress = Math.min(1, (System.currentTimeMillis() - tile.openTime) / openDuration);
            lid.rotateAngleY = (progress * 0.1f * ((float) Math.PI / 2F));

            matrixStack.translate(0,0, 0.3f * progress);
            lid.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            matrixStack.translate(0,0, -0.3f * progress);

        } else {
            lid.rotateAngleY = 0;
            lid.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
        }
    }

    private void renderLocks(ForgedContainerTile tile, float partialTicks, MatrixStack matrixStack, IVertexBuilder vertexBuilder,
            int combinedLight, int combinedOverlay) {
        Boolean[] locked = tile.isLocked();
        for (int i = 0; i < locks.length; i++) {
            if (locked[i]) {
                locks[i].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            }
        }
    }
}
