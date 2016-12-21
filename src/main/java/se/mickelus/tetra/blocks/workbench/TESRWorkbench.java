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

    private IModel model;
    private IBakedModel bakedModel;

    private RenderItem itemRenderer;

    private GuiWorkbench gui;

    public TESRWorkbench() {
        itemRenderer = Minecraft.getMinecraft().getRenderItem();

        gui = new GuiWorkbench();

        try {
            // Manually load our rotating model here
            model = ModelLoaderRegistry.getModel(new ResourceLocation(TetraMod.MOD_ID, "block/workbench"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private IBakedModel getBakedModel() {
        // Since we cannot bake in preInit() we do lazy baking of the model as soon as we need it
        // for rendering
        if (bakedModel == null) {
            bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        return bakedModel;
    }

    @Override
    public void renderTileEntityAt(TileEntityWorkbench te, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        // Translate to the location of our tile entity
        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();

        if (te.getItemStack() != null) {
            adjustLight(te);
            gui.setTileEntity(te);
            gui.draw();
            //renderHud(te.getItemStack().getDisplayName());
        }

        // Render the rotating handles
        //renderHandles(te);

        // Render our item
        renderItem(te);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();

    }

    private void renderHandles(TileEntityWorkbench te) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(.5, 1/8f, .5);
        long angle = (System.currentTimeMillis() / 10) % 360;
        // GlStateManager.rotate(angle, 0, 1, 0);

        RenderHelper.disableStandardItemLighting();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        World world = te.getWorld();
        // Translate back to local view coordinates so that we can do the acual rendering here
        GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                world,
                getBakedModel(),
                world.getBlockState(te.getPos()),
                te.getPos(),
                Tessellator.getInstance().getBuffer(),
                false);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void renderItem(TileEntityWorkbench te) {
        ItemStack stack = te.getItemStack();
        if (stack != null) {

            GlStateManager.pushMatrix();
            GlStateManager.translate(.5, 1.44, .5);
            GlStateManager.scale(.3f, .3f, .3f);

            rotateModel();

            applyCorrections(stack);

            adjustLight(te);

            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

            GlStateManager.popMatrix();
        }
    }

    private void adjustLight(TileEntityWorkbench te) {
        getWorld().getBlockState(te.getPos()).getPackedLightmapCoords(getWorld(), te.getPos());
        int i = getWorld().getCombinedLight(te.getPos().offset(EnumFacing.UP), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
    }

    private void rotateModel() {
        long angle = (System.currentTimeMillis() / 20) % 360;
        GlStateManager.rotate(angle, 0, 1, 0);
    }

    private void applyCorrections(ItemStack stack) {
        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, getWorld(), null);
        if (!model.isGui3d()) {
            GlStateManager.scale(.8f, .8f, .8f);
            GlStateManager.translate(0, 0.13, 0);
        }
    }
}
