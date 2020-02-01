package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.network.PacketHandler;

public class JumpHandlerBooster {

    private final Minecraft mc;

    private KeyBinding jumpKey;

    private boolean wasJumpKeyDown = false;

    public JumpHandlerBooster(Minecraft mc) {
        this.mc = mc;
        jumpKey = mc.gameSettings.keyBindJump;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.isGameFocused()) {
            if (jumpKey.isKeyDown() && mc.player.onGround && mc.player.isSneaking()) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(true, true);
                PacketHandler.sendToServer(packet);
            } else if (jumpKey.isKeyDown() && !wasJumpKeyDown && !mc.player.onGround) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(true);
                PacketHandler.sendToServer(packet);
            } else if (!jumpKey.isKeyDown() && wasJumpKeyDown) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(false);
                PacketHandler.sendToServer(packet);
            }

            wasJumpKeyDown = jumpKey.isKeyDown();
        }
    }


}
