package se.mickelus.tetra.blocks.workbench;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
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

            // Translate to the location of our tile entity
            GlStateManager.translated(x, y, z);

            // Render our item
            renderItem(itemStack);

            GlStateManager.popMatrix();
        }
    }

    private void renderItem(ItemStack itemStack) {
        GlStateManager.translated(.5, 0.94, .5);

        applyCorrections(itemStack);

        Minecraft.getInstance().getItemRenderer().renderItem(itemStack, TransformType.FIXED);
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
