package se.mickelus.tetra.items.modular;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

@OnlyIn(Dist.CLIENT)
public class ThrownModularItemRenderer extends EntityRenderer<ThrownModularItemEntity> {

    public ThrownModularItemRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(ThrownModularItemEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int packedLightIn) {
        matrixStack.push();

        Item item = entity.getArrowStack().getItem();
        if (item instanceof ModularSingleHeadedItem) {
            transformSingleHeaded(entity, partialTicks, matrixStack);
        } else if (item instanceof ModularDoubleHeadedItem) {
            transformDoubleHeaded(entity, partialTicks, matrixStack);
        } else if (item instanceof ModularShieldItem) {
            transformShield(entity, partialTicks, matrixStack);
        }

        Minecraft.getInstance().getItemRenderer().renderItem(entity.getArrowStack(), ItemCameraTransforms.TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer);

        matrixStack.pop();
        super.render(entity, entityYaw, partialTicks, matrixStack, renderTypeBuffer, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(ThrownModularItemEntity entity) {
        return null;
    }


    private void transformSingleHeaded(ThrownModularItemEntity entity, float partialTicks, MatrixStack matrixStack) {
        matrixStack.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F));
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch) + 135.0F));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(180.0F));
        matrixStack.translate(.3f, -.3f, 0);
    }

    private void transformDoubleHeaded(ThrownModularItemEntity entity, float partialTicks, MatrixStack matrixStack) {
        if (entity.hasDealtDamage()) {
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch) + 135.0F));
        } else {
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch) + entity.ticksExisted + partialTicks));
        }

        matrixStack.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(180.0F));
        matrixStack.translate(.3f, -.3f, 0);
    }

    private void transformShield(ThrownModularItemEntity entity, float partialTicks, MatrixStack matrixStack) {
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)));

        if (entity.isOnGround()) {
            matrixStack.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F));
        } else {
            matrixStack.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) + (entity.ticksExisted + partialTicks) * 100));
        }

        matrixStack.rotate(Vector3f.XP.rotationDegrees(90.0F));
        matrixStack.translate(-0.2, 0, 0);
    }
}
