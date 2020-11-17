package se.mickelus.tetra.items.toolbelt.booster;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import se.mickelus.tetra.network.PacketHandler;

import static se.mickelus.tetra.items.toolbelt.OverlayToolbelt.bindingGroup;

public class JumpHandlerBooster {

    private final Minecraft mc;

    public static final KeyBinding BOOST_BINDING = new KeyBinding("toolbelt.binding.boost", KeyConflictContext.IN_GAME, Keyboard.KEY_SPACE, bindingGroup);

    private boolean wasBoostKeyDown = false;

    public JumpHandlerBooster(Minecraft mc) {
        this.mc = mc;
        ClientRegistry.registerKeyBinding(BOOST_BINDING);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.inGameHasFocus) {
            if (BOOST_BINDING.isKeyDown() && mc.player.onGround && mc.player.isSneaking()) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(true, true);
                PacketHandler.sendToServer(packet);
            } else if (BOOST_BINDING.isKeyDown() && !wasBoostKeyDown && !mc.player.onGround) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(true);
                PacketHandler.sendToServer(packet);
            } else if (!BOOST_BINDING.isKeyDown() && wasBoostKeyDown) {
                UpdateBoosterPacket packet = new UpdateBoosterPacket(false);
                PacketHandler.sendToServer(packet);
            }

            wasBoostKeyDown = BOOST_BINDING.isKeyDown();
        }
    }


}
