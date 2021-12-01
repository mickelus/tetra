package se.mickelus.tetra.blocks.rack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class RackTESR implements BlockEntityRenderer<RackTile> {

    private ItemRenderer itemRenderer;

    public RackTESR(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(RackTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            Direction direction = tile.getBlockState().getValue(RackBlock.facingProp);
            Direction itemDirection = direction.getCounterClockWise();

            matrixStack.pushPose();
            matrixStack.translate(0.5 - direction.getStepX() * 0.36, 0.7, 0.5 - direction.getStepZ() * 0.36);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack itemStack = handler.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    matrixStack.pushPose();
                    matrixStack.translate(itemDirection.getStepX() * (i - 0.5), 0, itemDirection.getStepZ() * (i - 0.5));
                    matrixStack.mulPose(direction.getRotation());
                    renderItemStack(tile.getLevel(), itemStack, matrixStack, buffer, combinedLight, combinedOverlay);
                    matrixStack.popPose();
                }
            }
            matrixStack.popPose();
        });
    }

    private void renderItemStack(Level world, ItemStack itemStack, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        if (itemStack != null && !itemStack.isEmpty()) {

            BakedModel model = itemRenderer.getModel(itemStack, world, null, combinedLight);

            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

            if (itemStack.getItem() instanceof ModularShieldItem) {
                matrixStack.translate(-0.25, 0, 0.16);
                matrixStack.scale(2, 2, 2);
            } else if (itemStack.getItem() instanceof ModularBladedItem || itemStack.getItem() instanceof SwordItem) {
                matrixStack.translate(0, -0.2, 0);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(135.0F));
            } else if (itemStack.getItem() instanceof ModularCrossbowItem || itemStack.getItem() instanceof CrossbowItem) {
                matrixStack.translate(0, -0.2, 0);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(225.0F));
            } else if (model.isGui3d()) {
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-45.0F));
            } else {
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-45.0F));
            }

            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED, combinedLight, combinedOverlay,
                    matrixStack, buffer, 0); // FIXME: what does last int do? wasnt there on 1.16

        }
    }
}
