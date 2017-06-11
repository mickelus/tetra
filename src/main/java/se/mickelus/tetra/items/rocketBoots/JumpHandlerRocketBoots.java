package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import se.mickelus.tetra.network.PacketPipeline;

public class JumpHandlerRocketBoots {

    private final Minecraft mc;

    private KeyBinding jumpKey;

    private boolean wasJumpKeyDown = false;

    public  JumpHandlerRocketBoots(Minecraft mc) {
        this.mc = mc;
        jumpKey = mc.gameSettings.keyBindJump;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.inGameHasFocus) {
            if (jumpKey.isKeyDown() && mc.player.onGround && mc.player.isSneaking()) {
                UpdateBoostPacket packet = new UpdateBoostPacket(true, true);
                PacketPipeline.instance.sendToServer(packet);
            } else if (jumpKey.isKeyDown() && !wasJumpKeyDown && !mc.player.onGround) {
                UpdateBoostPacket packet = new UpdateBoostPacket(true);
                PacketPipeline.instance.sendToServer(packet);
            } else if (!jumpKey.isKeyDown() && wasJumpKeyDown) {
                UpdateBoostPacket packet = new UpdateBoostPacket(false);
                PacketPipeline.instance.sendToServer(packet);
            }

            wasJumpKeyDown = jumpKey.isKeyDown();
        }
    }


}
