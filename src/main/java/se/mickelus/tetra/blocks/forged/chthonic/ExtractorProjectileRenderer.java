package se.mickelus.tetra.blocks.forged.chthonic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ExtractorProjectileRenderer extends EntityRenderer<ExtractorProjectileEntity> {
    private static BlockRenderDispatcher blockRenderer;

    public ExtractorProjectileRenderer(EntityRendererProvider.Context manager) {
        super(manager);

        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(ExtractorProjectileEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int packedLightIn) {
        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        matrixStack.translate(-.3f, -.1f, -.45f);

        BakedModel model = blockRenderer.getBlockModelShaper().getBlockModel(ChthonicExtractorBlock.instance.defaultBlockState());
        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), renderTypeBuffer.getBuffer(Sheets.solidBlockSheet()),
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
