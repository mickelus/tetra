package se.mickelus.tetra.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerPlayNetHandler {

    @Inject(at = @At("TAIL"), method = "processPlayer")
    private void processPlayer(ServerboundMovePlayerPacket packet, CallbackInfo callback) {
        if (getInstance().player.hasEffect(SuspendPotionEffect.instance)) {
            setFloating(false);
        }
    }

    private ServerGamePacketListenerImpl getInstance() {
        return ((ServerGamePacketListenerImpl) (Object) this);
    }

    @Accessor
    public abstract void setFloating(boolean floating);
}
