package se.mickelus.tetra.generation;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.tileentity.StructureTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.data.DataManager;

import java.util.Arrays;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ExtendedStructureTESR extends StructureTileEntityRenderer {

    @Override
    public void render(StructureBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);

        BlockPos rel = te.getPosition();

        Optional.ofNullable(DataManager.featureData.getData(new ResourceLocation(te.getName())))
                .ifPresent(feature -> renderFeatureInfo(feature, x + rel.getX(), y + rel.getY(), z + rel.getZ()));
    }

    private void renderFeatureInfo(FeatureParameters feature, double x, double y, double z) {
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        setLightmapDisabled(true);
        GlStateManager.lineWidth(3);

        BlockPos origin = feature.origin;

        AxisAlignedBB aabb = new AxisAlignedBB(x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5,
                x + origin.getX() + 0.5, y + origin.getY() + 0.5, z + origin.getZ() + 0.5);

        // draw center box
        WorldRenderer.drawSelectionBoundingBox(aabb.grow(0.1), 0, 0, 0, 1);
        DebugRenderer.func_217730_a(aabb.grow(0.1), 1, 1, 1, 0.6f);

        // draw outline
        WorldRenderer.drawSelectionBoundingBox(aabb.grow(0.5030000000949949026D), 1, 0, 1, 1);

        Arrays.stream(feature.children).forEach(featureChild -> renderChild(featureChild, x, y, z));

        Arrays.stream(feature.loot).forEach(featureLoot -> renderLoot(featureLoot, x, y, z));

        GlStateManager.lineWidth(1.0F);
        setLightmapDisabled(false);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
//        GlStateManager.enableDepthTest();
//        GlStateManager.depthMask(true);
        GlStateManager.enableFog();
    }

    private void renderChild(FeatureChild featureChild, double x, double y, double z) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        BlockPos offset = featureChild.offset;

        AxisAlignedBB aabb = new AxisAlignedBB(x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5,
                x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5);

        // build outline box
        WorldRenderer.drawSelectionBoundingBox(aabb.grow(0.5020000000949949026D), 1, 1, 0, 1);

        // build arrow
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        Vec3i facing = featureChild.facing.getDirectionVec();
        bufferBuilder.pos(x + offset.getX() + 0.5, y + offset.getY() + 0.5, z + offset.getZ() + 0.5).color(0.0F, 0.0F, 0.0F, 0.0F).endVertex();
        bufferBuilder.pos(
                x + offset.getX() + 0.5 + 0.3 * facing.getX(),
                y + offset.getY() + 0.5 + 0.3 * facing.getY(),
                z + offset.getZ() + 0.5 + 0.3 * facing.getZ())
                .color(1, 1, 1, 1.0F).endVertex();

        tessellator.draw();

        // draw middle block
        DebugRenderer.func_217730_a(aabb.grow(0.1), 0, 0, 0, 0.8f);
    }

    private void renderLoot(FeatureLoot featureLoot, double x, double y, double z) {
        BlockPos offset = featureLoot.position;

        AxisAlignedBB aabb = new AxisAlignedBB(x + offset.getX(), y + offset.getY(), z + offset.getZ(),
                x + offset.getX() + 0.2, y + offset.getY() + 0.2, z + offset.getZ() + 0.2).offset(-0.1, -0.1, -0.1);

        // draw bottom blocks
        DebugRenderer.func_217730_a(aabb, 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb, 0, 1, 0, 1);
        DebugRenderer.func_217730_a(aabb.offset(1, 0, 0), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(1, 0, 0), 0, 1, 0, 1);
        DebugRenderer.func_217730_a(aabb.offset(0, 0, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(0, 0, 1), 0, 1, 0, 1);
        DebugRenderer.func_217730_a(aabb.offset(1, 0, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(1, 0, 1), 0, 1, 0, 1);

        // draw top blocks
        DebugRenderer.func_217730_a(aabb.offset(0, 1, 0), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(0, 1, 0), 0, 1, 0, 1);
        DebugRenderer.func_217730_a(aabb.offset(0, 1, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(0, 1, 1), 0, 1, 0, 1);
        DebugRenderer.func_217730_a(aabb.offset(1, 1, 0), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(1, 1, 0), 0, 1, 0, 1);
        DebugRenderer.func_217730_a(aabb.offset(1, 1, 1), 0, 1, 0, 0.2f);
        WorldRenderer.drawSelectionBoundingBox(aabb.offset(1, 1, 1), 0, 1, 0, 1);
    }
}
