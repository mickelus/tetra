package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import se.mickelus.tetra.items.toolbelt.EquipToolbeltItemPacket;
import se.mickelus.tetra.items.toolbelt.InventoryToolbelt;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.network.PacketPipeline;

public class OverlayToolbelt {

    private final Minecraft mc;

    private final String bindingDescription = "se.mickelus.tetra.toolbeltDescription";
    private final String bindingGroup = "key.categories.inventory";
    private int bindingKey = Keyboard.KEY_V;
    private KeyBinding key;

    private boolean wasKeyDown = false;

    private OverlayGuiToolbelt gui;

    public OverlayToolbelt(Minecraft mc) {
        this.mc = mc;

        gui = new OverlayGuiToolbelt(mc);

        key = new KeyBinding(bindingDescription, bindingKey, bindingGroup);
        ClientRegistry.registerKeyBinding(key);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.inGameHasFocus) {
            if (key.isKeyDown() && !wasKeyDown) {
                showView();
                wasKeyDown = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        if (!key.isKeyDown() && wasKeyDown) {
            hideView();
            wasKeyDown = false;
        }

        gui.draw();
    }

    private void showView() {
        boolean canOpen = updateGuiData();
        if (canOpen) {
            mc.inGameHasFocus = false;
            mc.mouseHelper.ungrabMouseCursor();
        }
    }

    private void hideView() {
        mc.inGameHasFocus = true;
        gui.setVisible(false);
        mc.mouseHelper.grabMouseCursor();

        int focusIndex = findIndex();
        if (focusIndex != -1) {
            equipToolbeltItem(focusIndex);
        }
    }

    private void equipToolbeltItem(int toolbeltItemIndex) {
        EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(toolbeltItemIndex);
        PacketPipeline.instance.sendToServer(packet);
    }

    private boolean updateGuiData() {
        InventoryToolbelt inventoryToolbelt = UtilToolbelt.findToolbeltInventory(mc.player);
        if (inventoryToolbelt != null) {
            gui.setInventoryToolbelt(inventoryToolbelt);
            gui.setVisible(true);
            return true;
        }

        return false;
    }

    private int findIndex() {
        return gui.getFocus();
    }
}
