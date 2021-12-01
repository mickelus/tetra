package se.mickelus.tetra.blocks.forged.extractor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class CoreExtractorPistonTESR implements BlockEntityRenderer<CoreExtractorPistonTile> {
    private static BlockRenderDispatcher blockRenderer;

    public CoreExtractorPistonTESR(BlockEntityRenderDispatcher dispatcher) {
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(CoreExtractorPistonTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {

        BlockState state = CoreExtractorPistonBlock.instance.defaultBlockState();

        double offset = tile.getProgress(partialTicks);

        if (offset > 0.98) {
            // 49 = 0.98 / ( 1 - 0.98)
            offset = -49 * offset + 49;
        }

        BlockState shaftState = state.setValue(CoreExtractorPistonBlock.hackProp, true);
        BakedModel shaftModel = blockRenderer.getBlockModelShaper().getBlockModel(shaftState);
        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(Sheets.solidBlockSheet()),
                shaftState, shaftModel, 1f, 1f, 1f, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        matrixStack.translate(0, offset, 0);

        BlockState coverState = state.setValue(CoreExtractorPistonBlock.hackProp, false);
        BakedModel coverModel = blockRenderer.getBlockModelShaper().getBlockModel(coverState);
        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(Sheets.solidBlockSheet()),
                coverState, coverModel, 1f, 1f, 1f, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
    }
}
