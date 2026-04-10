package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.effect.EffectHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class JumpHandlerSuspend {
    private final Minecraft mc;

    private final KeyMapping jumpKey;
    private boolean wasJumpKeyDown = false;

    public JumpHandlerSuspend(Minecraft mc) {
        this.mc = mc;
        jumpKey = mc.options.keyJump;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyInput(InputEvent.Key event) {
        if (mc.isWindowActive()) {
            Player player = mc.player;
            if (jumpKey.isDown() && !wasJumpKeyDown
                    && !player.onGround() && !player.isCreative() && !player.isSpectator()) {
                boolean isSuspended = player.hasEffect(EffectHelper.effectHolder(SuspendPotionEffect.instance));
                if (!isSuspended || player.isShiftKeyDown()) {
                    SuspendEffect.toggleSuspend(player, !isSuspended);
                    TetraMod.packetHandler.sendToServer(new ToggleSuspendPacket(!isSuspended));
                }
            }
            wasJumpKeyDown = jumpKey.isDown();
        }
    }


}
