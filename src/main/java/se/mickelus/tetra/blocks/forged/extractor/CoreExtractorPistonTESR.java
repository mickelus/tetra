package se.mickelus.tetra.blocks.forged.extractor;

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


public class CoreExtractorPistonTESR extends TileEntityRenderer<CoreExtractorPistonTile> {
    private static BlockRendererDispatcher blockRenderer;

    public CoreExtractorPistonTESR() {}

    @Override
    public void renderTileEntityFast(CoreExtractorPistonTile te, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {
        if(blockRenderer == null) {
            blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        }

        BlockPos pos = te.getPos();

        IEnviromentBlockReader world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        BlockState state = CoreExtractorPistonBlock.instance.getDefaultState();

        double offset = te.getProgress(partialTicks);

        if (offset > 0.98) {
            // 49 = 0.98 / ( 1 - 0.98)
            offset = -49 * offset + 49;
        }


        buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        IBakedModel shaftModel = blockRenderer.getBlockModelShapes().getModel(state.with(CoreExtractorPistonBlock.hackProp, true));
        blockRenderer.getBlockModelRenderer().renderModel(world, shaftModel, state, pos, buffer, false, new Random(), 42,
                EmptyModelData.INSTANCE);

        buffer.setTranslation(x - pos.getX(), y - pos.getY() + offset, z - pos.getZ());
        IBakedModel coverModel = blockRenderer.getBlockModelShapes().getModel(state.with(CoreExtractorPistonBlock.hackProp, false));
        blockRenderer.getBlockModelRenderer().renderModel(world, coverModel, state, pos, buffer, false, new Random(), 42,
                EmptyModelData.INSTANCE);
    }
}
