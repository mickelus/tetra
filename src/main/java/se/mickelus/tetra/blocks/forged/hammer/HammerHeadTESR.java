package se.mickelus.tetra.blocks.forged.hammer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class HammerHeadTESR implements BlockEntityRenderer<HammerHeadTile> {
    private static BlockRenderDispatcher blockRenderer;
    private static final float animationDuration = 400;
    private static final float unjamDuration = 800;

    public HammerHeadTESR(BlockEntityRendererProvider.Context context) {
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    // todo 1.15: ripped out
    @Override
    public void render(HammerHeadTile tile, float v, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {

        BakedModel model = blockRenderer.getBlockModelShaper().getBlockModel(HammerHeadBlock.instance.defaultBlockState());

        double offset = Mth.clamp((1d * System.currentTimeMillis() - tile.getActivationTime()) / animationDuration, 0, 0.875);

        offset = Math.min(offset, Mth.clamp(0.25 + (1d * System.currentTimeMillis() - tile.getUnjamTime()) / unjamDuration, 0, 1) - 0.125);

        if (tile.isJammed()) {
            offset = Math.min(offset, 0.25f);
        }

        matrixStack.translate(0, offset, 0);

        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(Sheets.solidBlockSheet()),
                HammerHeadBlock.instance.defaultBlockState(), model, 1f, 1f, 1f, combinedLight, combinedOverlay,
                EmptyModelData.INSTANCE);
    }
}
