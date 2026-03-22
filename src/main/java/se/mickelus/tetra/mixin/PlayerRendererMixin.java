package se.mickelus.tetra.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;

@OnlyIn(Dist.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(at = @At("HEAD"), method = "getArmPose", cancellable = true)
    private static void getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> callback) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.isUsingItem()
                && !player.swinging
                && ModularCrossbowItemImpl.instance.equals(itemStack.getItem())
                && ((ModularCrossbowItemImpl) itemStack.getItem()).isLoaded(itemStack)) {
            callback.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            callback.cancel();
        }
    }
}
