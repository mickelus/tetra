package se.mickelus.tetra.blocks.forged.chthonic;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class ExtractorProjectileRenderer extends EntityRenderer<ExtractorProjectileEntity> {
    private static BlockRendererDispatcher blockRenderer;

    public ExtractorProjectileRenderer(EntityRendererManager manager) {
        super(manager);

        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(ExtractorProjectileEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int packedLightIn) {
        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot) + 90.0F));
        matrixStack.translate(-.3f, -.1f, -.45f);

        IBakedModel model = blockRenderer.getBlockModelShaper().getBlockModel(ChthonicExtractorBlock.instance.defaultBlockState());
        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), renderTypeBuffer.getBuffer(Atlases.solidBlockSheet()),
                ChthonicExtractorBlock.instance.defaultBlockState(), model, 1, 1, 1, packedLightIn, OverlayTexture.NO_OVERLAY,
                EmptyModelData.INSTANCE);

        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, renderTypeBuffer, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ExtractorProjectileEntity entity) {
        return null;
    }
}
