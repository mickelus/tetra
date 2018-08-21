package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRWorkbench extends TileEntitySpecialRenderer<TileEntityWorkbench> {

    private RenderItem itemRenderer;

//    private GuiHudWorkbench gui;

    public TESRWorkbench() {
        itemRenderer = Minecraft.getMinecraft().getRenderItem();
    }

    @Override
    public void render(TileEntityWorkbench te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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
            GlStateManager.translate(.5, 0.94, .5);

            applyCorrections(stack);

            adjustLight(te);

            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
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
            GlStateManager.translate(0, 0.073, 0.1);
            GlStateManager.scale(.8f, .8f, .8f);
            GlStateManager.rotate(-90, 1, 0, 0);
        }
    }
}
