package se.mickelus.tetra.blocks.workbench;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorkbenchTESR extends TileEntityRenderer<WorkbenchTile> {

    private ItemRenderer itemRenderer;

    public WorkbenchTESR() {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(WorkbenchTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        ItemStack itemStack = te.getTargetItemStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            GlStateManager.pushMatrix();

            // Translate to the location of the te position relative the player
            GlStateManager.translated(x, y, z);

            // Render our item
            int i = Minecraft.getInstance().world.getCombinedLight(te.getPos().up(), 0);
            int skyLight = i % 65536;
            int blockLight = i / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, skyLight, blockLight);
            GlStateManager.enableLighting();
            renderItem(itemStack);
            GlStateManager.disableLighting();

            GlStateManager.popMatrix();
        }
    }

    private void renderItem(ItemStack itemStack) {
        GlStateManager.translated(.5, 0.94, .5);

        applyCorrections(itemStack);

        itemRenderer.renderItem(itemStack, TransformType.GROUND);
    }


    private void applyCorrections(ItemStack stack) {
        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, getWorld(), null);
        if (!model.isGui3d()) {
            GlStateManager.translated(0, 0.073, 0.1);
            GlStateManager.scaled(.8f, .8f, .8f);
            GlStateManager.rotated(-90, 1, 0, 0);
        }
    }
}
