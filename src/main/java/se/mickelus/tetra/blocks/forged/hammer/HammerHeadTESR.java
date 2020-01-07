package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

public class HammerHeadTESR extends TileEntityRenderer<HammerHeadTile> {
    private static BlockRendererDispatcher blockRenderer;
    private static final float animationDuration = 400;

    public HammerHeadTESR() { }

    @Override
    public void renderTileEntityFast(HammerHeadTile te, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {
        if(blockRenderer == null) {
            blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        }

        BlockPos pos = te.getPos();
        IEnviromentBlockReader world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        IBakedModel model = blockRenderer.getBlockModelShapes().getModel(HammerHeadBlock.instance.getDefaultState());

        double offset = Math.min(1, Math.max(0, (1d * System.currentTimeMillis() - te.getActivationTime()) / animationDuration)) - 0.125;
        buffer.setTranslation(x - pos.getX(), y - pos.getY() + offset, z - pos.getZ());

        blockRenderer.getBlockModelRenderer().renderModel(world, model, HammerHeadBlock.instance.getDefaultState(), pos, buffer,
                false, new Random(), 42, EmptyModelData.INSTANCE);

    }
}
