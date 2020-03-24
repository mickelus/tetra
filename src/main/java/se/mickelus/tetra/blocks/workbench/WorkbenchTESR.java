package se.mickelus.tetra.blocks.workbench;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorkbenchTESR extends TileEntityRenderer<WorkbenchTile> {

    private ItemRenderer itemRenderer;

    public WorkbenchTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    // todo 1.15: this changed quite alot, check that it still works
    @Override
    public void render(WorkbenchTile workbenchTile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        ItemStack itemStack = workbenchTile.getTargetItemStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            matrixStack.push();
            matrixStack.translate(0.5D, 0.44921875D, 0.5D);
            matrixStack.rotate(Vector3f.XP.rotationDegrees(90.0F));
            matrixStack.translate(-0.3125D, -0.3125D, 0.0D);
            matrixStack.scale(0.375F, 0.375F, 0.375F);
            Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED, combinedLight, combinedOverlay, matrixStack, buffer);
            matrixStack.pop();
        }
    }

//    private void renderItem(ItemStack itemStack) {
//        GlStateManager.translated(.5, 0.94, .5);
//
//        applyCorrections(itemStack);
//
//        itemRenderer.renderItem(itemStack, TransformType.GROUND);
//    }
//
//
//    private void applyCorrections(ItemStack stack) {
//        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, getWorld(), null);
//        if (!model.isGui3d()) {
//            GlStateManager.translated(0, 0.073, 0.1);
//            GlStateManager.scaled(.8f, .8f, .8f);
//            GlStateManager.rotated(-90, 1, 0, 0);
//        }
//    }
}
