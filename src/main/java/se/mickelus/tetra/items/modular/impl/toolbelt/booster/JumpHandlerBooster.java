package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.TetraMod;

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
            if (jumpKey.isKeyDown() && mc.player.isOnGround() && mc.player.isCrouching()) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(true, true);
                TetraMod.packetHandler.sendToServer(packet);
            } else if (jumpKey.isKeyDown() && !wasJumpKeyDown && !mc.player.isOnGround()) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(true);
                TetraMod.packetHandler.sendToServer(packet);
            } else if (!jumpKey.isKeyDown() && wasJumpKeyDown) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(false);
                TetraMod.packetHandler.sendToServer(packet);
            }

            wasJumpKeyDown = jumpKey.isKeyDown();
        }
    }
}
