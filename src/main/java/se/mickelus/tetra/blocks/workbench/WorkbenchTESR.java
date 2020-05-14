package se.mickelus.tetra.blocks.workbench;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

@OnlyIn(Dist.CLIENT)
public class WorkbenchTESR extends TileEntityRenderer<WorkbenchTile> {

    private ItemRenderer itemRenderer;

    public WorkbenchTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(WorkbenchTile workbenchTile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        ItemStack itemStack = workbenchTile.getTargetItemStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            matrixStack.push();

            IBakedModel model = itemRenderer.getItemModelWithOverrides(itemStack, workbenchTile.getWorld(), null);
            if (itemStack.getItem() instanceof ModularShieldItem) {
                matrixStack.translate(0.375, 0.9125, 0.5);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(90.0F));
//                matrixStack.scale(0.5f, 0.5f, 0.5f);
            } else if (model.isGui3d()) {
                matrixStack.translate(0.5, 1.125, 0.5);
                matrixStack.scale(.5f, .5f, .5f);
            } else {
                matrixStack.translate(0.5, 1.0125, 0.5);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(90.0F));
                matrixStack.scale(0.5f, 0.5f, 0.5f);
            }

            Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED,
                    WorldRenderer.getCombinedLight(workbenchTile.getWorld(), workbenchTile.getPos().up()),
                    combinedOverlay, matrixStack, buffer);

            matrixStack.pop();
        }
    }
}
