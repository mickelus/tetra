package se.mickelus.tetra.items.modular;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

@OnlyIn(Dist.CLIENT)
public class ThrownModularItemRenderer extends EntityRenderer<ThrownModularItemEntity> {

    public ThrownModularItemRenderer(EntityRenderDispatcher manager) {
        super(manager);
    }

    @Override
    public void render(ThrownModularItemEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int packedLightIn) {
        matrixStack.pushPose();

        Item item = entity.getPickupItem().getItem();
        if (item instanceof ModularSingleHeadedItem) {
            transformSingleHeaded(entity, partialTicks, matrixStack);
        } else if (item instanceof ModularDoubleHeadedItem) {
            transformDoubleHeaded(entity, partialTicks, matrixStack);
        } else if (item instanceof ModularBladedItem) {
            transformBlade(entity, partialTicks, matrixStack);
        } else if (item instanceof ModularShieldItem) {
            transformShield(entity, partialTicks, matrixStack);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(entity.getPickupItem(), ItemTransforms.TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer);

        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, renderTypeBuffer, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownModularItemEntity entity) {
        return null;
    }

    private void transformSingleHeaded(ThrownModularItemEntity entity, float partialTicks, PoseStack matrixStack) {
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.xRot) + 135.0F));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        matrixStack.translate(.3f, -.3f, 0);
    }

    private void transformDoubleHeaded(ThrownModularItemEntity entity, float partialTicks, PoseStack matrixStack) {
        if (entity.hasDealtDamage()) {
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.xRot) + 135.0F));
        } else {
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.xRot) + entity.tickCount + partialTicks));
        }

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        matrixStack.translate(.3f, -.3f, 0);
    }

    private void transformBlade(ThrownModularItemEntity entity, float partialTicks, PoseStack matrixStack) {
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.xRot) + 135.0F));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
    }

    private void transformShield(ThrownModularItemEntity entity, float partialTicks, PoseStack matrixStack) {
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.xRot)));

        if (entity.isOnGround()) {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        } else {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.yRot) + (entity.tickCount + partialTicks) * 100));
        }

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        matrixStack.translate(-0.2, 0, 0);
    }
}
