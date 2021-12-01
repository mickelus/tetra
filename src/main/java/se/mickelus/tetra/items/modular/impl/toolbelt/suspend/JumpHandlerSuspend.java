package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.TetraMod;

public class JumpHandlerSuspend {
    private final Minecraft mc;

    private KeyMapping jumpKey;
    private boolean wasJumpKeyDown = false;

    public JumpHandlerSuspend(Minecraft mc) {
        this.mc = mc;
        jumpKey = mc.options.keyJump;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.isWindowActive()) {
            Player player = mc.player;
            if (jumpKey.isDown() && !wasJumpKeyDown
                    && !player.isOnGround() && !player.isCreative() && !player.isSpectator()) {
                boolean isSuspended = player.hasEffect(SuspendPotionEffect.instance);
                if (!isSuspended || player.isShiftKeyDown()) {
                    SuspendEffect.toggleSuspend(player, !isSuspended);
                    TetraMod.packetHandler.sendToServer(new ToggleSuspendPacket(!isSuspended));
                }
            }
            wasJumpKeyDown = jumpKey.isDown();
        }
    }


}
