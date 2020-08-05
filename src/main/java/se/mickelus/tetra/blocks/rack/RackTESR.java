package se.mickelus.tetra.blocks.rack;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

@OnlyIn(Dist.CLIENT)
public class RackTESR extends TileEntityRenderer<RackTile> {

    private ItemRenderer itemRenderer;

    public RackTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(RackTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            Direction direction = tile.getBlockState().get(RackBlock.facingProp);
            Direction itemDirection = direction.rotateYCCW();

            matrixStack.push();
            matrixStack.translate(0.5 - direction.getXOffset() * 0.36, 0.7, 0.5 - direction.getZOffset() * 0.36);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack itemStack = handler.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    matrixStack.push();
                    matrixStack.translate(itemDirection.getXOffset() * (i - 0.5), 0, itemDirection.getZOffset() * (i - 0.5));
                    matrixStack.rotate(direction.getRotation());
                    renderItemStack(tile.getWorld(), itemStack, matrixStack, buffer, combinedLight, combinedOverlay);
                    matrixStack.pop();
                }
            }
            matrixStack.pop();
        });
    }

    private void renderItemStack(World world, ItemStack itemStack, MatrixStack matrixStack, IRenderTypeBuffer buffer,
            int combinedLight, int combinedOverlay) {
        if (itemStack != null && !itemStack.isEmpty()) {

            IBakedModel model = itemRenderer.getItemModelWithOverrides(itemStack, world, null);

            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));

            if (itemStack.getItem() instanceof ModularShieldItem) {
                matrixStack.translate(-0.25, 0, 0.16);
                matrixStack.scale(2, 2, 2);
            } else if (itemStack.getItem() instanceof ModularBladedItem || itemStack.getItem() instanceof SwordItem) {
                matrixStack.translate(0, -0.2, 0);
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(135.0F));
            } else if (model.isGui3d()) {
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(-45.0F));
            } else {
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(-45.0F));
            }

            Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED, combinedLight, combinedOverlay,
                    matrixStack, buffer);

        }
    }
}
