package se.mickelus.tetra.mixin;

import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.potion.Effects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;

@Mixin(ServerPlayNetHandler.class)
public abstract class MixinServerPlayNetHandler {

    @Inject(at = @At("TAIL"), method = "processPlayer")
    private void processPlayer(CPlayerPacket packet, CallbackInfo callback) {
        if (getInstance().player.isPotionActive(SuspendPotionEffect.instance)) {
            setFloating(false);
        }
    }

    private ServerPlayNetHandler getInstance() {
        return ((ServerPlayNetHandler) (Object) this);
    }

    @Accessor
    abstract void setFloating(boolean floating);
}
