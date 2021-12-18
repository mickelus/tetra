package se.mickelus.tetra.blocks.workbench;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class WorkbenchTESR implements BlockEntityRenderer<WorkbenchTile> {

    private ItemRenderer itemRenderer;

    public WorkbenchTESR(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(WorkbenchTile workbenchTile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ItemStack itemStack = workbenchTile.getTargetItemStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            matrixStack.pushPose();

            int renderId = (int) workbenchTile.getBlockPos().asLong();

            BakedModel model = itemRenderer.getModel(itemStack, workbenchTile.getLevel(), null, renderId);
            if (itemStack.getItem() instanceof ModularShieldItem) {
                matrixStack.translate(0.375, 0.9125, 0.5);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//                matrixStack.scale(0.5f, 0.5f, 0.5f);
            } else if (model.isGui3d()) {
                matrixStack.translate(0.5, 1.125, 0.5);
                matrixStack.scale(.5f, .5f, .5f);
            } else {
                matrixStack.translate(0.5, 1.0125, 0.5);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                matrixStack.scale(0.5f, 0.5f, 0.5f);
            }

            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED,
                    LevelRenderer.getLightColor(workbenchTile.getLevel(), workbenchTile.getBlockPos().above()),
                    combinedOverlay, matrixStack, buffer, renderId);

            matrixStack.popPose();
        }
    }
}
