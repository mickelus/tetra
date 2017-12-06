package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.hud.TileEntityHudRenderer;


@SideOnly(Side.CLIENT)
public class TESRWorkbench extends TileEntityHudRenderer<TileEntityWorkbench> {

    private RenderItem itemRenderer;

//    private GuiHudWorkbench gui;

    public TESRWorkbench() {
        itemRenderer = Minecraft.getMinecraft().getRenderItem();
    }

    @Override
    public void renderTileEntityFast(TileEntityWorkbench te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        // Translate to the location of our tile entity
        GlStateManager.translate(x, y, z);

        GlStateManager.disableRescaleNormal();

        // Render our item
        renderItem(te);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private void renderItem(TileEntityWorkbench te) {
        ItemStack stack = te.getStackInSlot(0);
        if (stack != null) {

            GlStateManager.pushMatrix();
            GlStateManager.translate(.5, 0.94, .5);

            applyCorrections(stack);

            adjustLight(te);

            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

            GlStateManager.popMatrix();
        }
    }

    private void adjustLight(TileEntityWorkbench te) {
        getWorld().getBlockState(te.getPos()).getPackedLightmapCoords(getWorld(), te.getPos());
        int combinedLight = getWorld().getCombinedLight(te.getPos().offset(EnumFacing.UP), 0);
        int x = combinedLight % 65536;
        int y = combinedLight / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)x, (float)y);
    }

    private void applyCorrections(ItemStack stack) {
        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, getWorld(), null);
        if (!model.isGui3d()) {
            GlStateManager.translate(0, 0.073, -0.1);
            GlStateManager.scale(.8f, .8f, .8f);
            GlStateManager.rotate(90, 1, 0, 0);
        }
    }
}
