package se.mickelus.tetra.blocks.forged.extractor;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

import java.util.Random;


public class CoreExtractorPistonTESR extends TileEntityRenderer<CoreExtractorPistonTile> {
    private static BlockRendererDispatcher blockRenderer;

    @ObjectHolder(TetraMod.MOD_ID + ":" + CoreExtractorPistonBlock.unlocalizedName)
    public static TileEntityType<CoreExtractorPistonTile> type;
    
    private Random random = new Random();

    public CoreExtractorPistonTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CoreExtractorPistonTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int i, int i1) {
        if(blockRenderer == null) {
            blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        }

        BlockPos pos = te.getPos();

        ChunkRenderCache world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        BlockState state = CoreExtractorPistonBlock.instance.getDefaultState();

        double offset = te.getProgress(partialTicks);

        if (offset > 0.98) {
            // 49 = 0.98 / ( 1 - 0.98)
            offset = -49 * offset + 49;
        }

        IBakedModel shaftModel = blockRenderer.getBlockModelShapes().getModel(state.with(CoreExtractorPistonBlock.hackProp, true));
        blockRenderer.getBlockModelRenderer().renderModel(world, shaftModel, state, pos, matrixStack,
                buffer.getBuffer(Atlases.getSolidBlockType()), false, random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

        matrixStack.translate(0, offset, 0);
        IBakedModel coverModel = blockRenderer.getBlockModelShapes().getModel(state.with(CoreExtractorPistonBlock.hackProp, false));
        blockRenderer.getBlockModelRenderer().renderModel(world, coverModel, state, pos, matrixStack,
                buffer.getBuffer(Atlases.getSolidBlockType()), false, random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
    }
}
