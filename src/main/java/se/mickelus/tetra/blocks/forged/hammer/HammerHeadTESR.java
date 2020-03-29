package se.mickelus.tetra.blocks.forged.hammer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HammerHeadTESR extends TileEntityRenderer<HammerHeadTile> {
    private static BlockRendererDispatcher blockRenderer;
    private static final float animationDuration = 400;

    public HammerHeadTESR(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);
    }

    // todo 1.15: ripped out
    @Override
    public void render(HammerHeadTile hammerHeadTile, float v, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int i, int i1) {
//        if(blockRenderer == null) {
//            blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
//        }
//
//        BlockPos pos = te.getPos();
//        IEnviromentBlockReader world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
//        IBakedModel model = blockRenderer.getBlockModelShapes().getModel(HammerHeadBlock.instance.getDefaultState());
//
//        double offset = Math.min(1, Math.max(0, (1d * System.currentTimeMillis() - te.getActivationTime()) / animationDuration)) - 0.125;
//        buffer.setTranslation(x - pos.getX(), y - pos.getY() + offset, z - pos.getZ());
//
//        blockRenderer.getBlockModelRenderer().renderModel(world, model, HammerHeadBlock.instance.getDefaultState(), pos, buffer,
//                false, new Random(), 42, EmptyModelData.INSTANCE);
    }
}
