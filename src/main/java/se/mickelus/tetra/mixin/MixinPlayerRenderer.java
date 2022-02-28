package se.mickelus.tetra.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer {
    @Inject(at = @At("HEAD"), method = "getArmPose", cancellable = true)
    private static void getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> callback) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.isUsingItem()
                && !player.swinging
                && ModularCrossbowItem.instance.equals(itemStack.getItem())
                && ((ModularCrossbowItem) itemStack.getItem()).isLoaded(itemStack)) {
            callback.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            callback.cancel();
        }
    }
}
