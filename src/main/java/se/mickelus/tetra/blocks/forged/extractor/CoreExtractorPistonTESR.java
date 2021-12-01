package se.mickelus.tetra.blocks.forged.extractor;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class CoreExtractorPistonTESR extends TileEntityRenderer<CoreExtractorPistonTile> {
    private static BlockRendererDispatcher blockRenderer;

    public CoreExtractorPistonTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(CoreExtractorPistonTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight,
            int combinedOverlay) {

        BlockState state = CoreExtractorPistonBlock.instance.defaultBlockState();

        double offset = tile.getProgress(partialTicks);

        if (offset > 0.98) {
            // 49 = 0.98 / ( 1 - 0.98)
            offset = -49 * offset + 49;
        }

        BlockState shaftState = state.setValue(CoreExtractorPistonBlock.hackProp, true);
        IBakedModel shaftModel = blockRenderer.getBlockModelShaper().getBlockModel(shaftState);
        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(Atlases.solidBlockSheet()),
                shaftState, shaftModel, 1f, 1f, 1f, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        matrixStack.translate(0, offset, 0);

        BlockState coverState = state.setValue(CoreExtractorPistonBlock.hackProp, false);
        IBakedModel coverModel = blockRenderer.getBlockModelShaper().getBlockModel(coverState);
        blockRenderer.getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(Atlases.solidBlockSheet()),
                coverState, coverModel, 1f, 1f, 1f, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
    }
}
