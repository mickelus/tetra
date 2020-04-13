package se.mickelus.tetra.blocks.forged.hammer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class HammerHeadTESR extends TileEntityRenderer<HammerHeadTile> {
    private static BlockRendererDispatcher blockRenderer;
    private static final float animationDuration = 400;

    public HammerHeadTESR(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);

        blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
    }

    // todo 1.15: ripped out
    @Override
    public void render(HammerHeadTile tile, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer,
            int combinedLight, int combinedOverlay) {

        IBakedModel model = blockRenderer.getBlockModelShapes().getModel(HammerHeadBlock.instance.getDefaultState());

        double offset = Math.min(1, Math.max(0, (1d * System.currentTimeMillis() - tile.getActivationTime()) / animationDuration)) - 0.125;
        matrixStack.translate(0, offset, 0);

        blockRenderer.getBlockModelRenderer().renderModel(matrixStack.getLast(), buffer.getBuffer(Atlases.getSolidBlockType()),
                HammerHeadBlock.instance.getDefaultState(), model, 1f, 1f, 1f, combinedLight, combinedOverlay,
                EmptyModelData.INSTANCE);
    }
}
