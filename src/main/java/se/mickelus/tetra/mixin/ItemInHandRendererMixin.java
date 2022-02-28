package se.mickelus.tetra.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 1), method = "renderArmWithItem")
    private void renderArmWithItem(AbstractClientPlayer player, float partialTicks, float interpolatedPitch, InteractionHand hand, float swingProgress,
            ItemStack itemStack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (ModularCrossbowItem.instance.equals(itemStack.getItem())) {
            tetraTransformCrossbow(player, partialTicks, interpolatedPitch, hand, swingProgress, itemStack, equipProgress, poseStack, buffer, light, ci);
        }
    }

    @Shadow
    protected abstract void applyItemArmAttackTransform(PoseStack p_109336_, HumanoidArm p_109337_, float p_109338_);

    @Shadow
    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float p_109385_) {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }

    private void tetraTransformCrossbow(AbstractClientPlayer player, float partialTicks, float interpolatedPitch, InteractionHand hand, float p_109376_,
            ItemStack itemStack, float p_109378_, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        boolean isMainhand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainhand ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isCharged = CrossbowItem.isCharged(itemStack);
        boolean flag2 = arm == HumanoidArm.RIGHT;
        int i = flag2 ? 1 : -1;
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            this.applyItemArmTransform(poseStack, arm, p_109378_);
            poseStack.translate(i * -0.4785682F, -0.094387F, 0.05731531F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(i * 65.3F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(i * -9.785F));
            float f9 = itemStack.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f13 = f9 /  ((ModularCrossbowItem) itemStack.getItem()).getReloadDuration(itemStack);
            if (f13 > 1.0F) {
                f13 = 1.0F;
            }

            if (f13 > 0.1F) {
                float f16 = Mth.sin((f9 - 0.1F) * 1.3F);
                float f3 = f13 - 0.1F;
                float f4 = f16 * f3;
                poseStack.translate((double)(f4 * 0.0F), (double)(f4 * 0.004F), (double)(f4 * 0.0F));
            }

            poseStack.translate(0, 0, f13 * 0.04);
            poseStack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
            poseStack.mulPose(Vector3f.YN.rotationDegrees((float)i * 45.0F));
        } else if (isCharged && p_109376_ < 0.001F && isMainhand) {
            poseStack.translate(i * -0.641864F, 0.0D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees((float)i * 10.0F));
        }
    }
}
