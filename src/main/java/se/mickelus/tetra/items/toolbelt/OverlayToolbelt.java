package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRoot;
import se.mickelus.tetra.network.PacketPipeline;

public class OverlayToolbelt {

    private final Minecraft mc;

    private final String bindingDescription = "se.mickelus.tetra.toolbeltDescription";
    private final String bindingGroup = "key.categories.inventory";
    private int bindingKey = Keyboard.KEY_V;
    private KeyBinding key;

    private boolean drawOverlay = false;

    private boolean canShow = false;

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
            if (key.isKeyDown() && !drawOverlay) {
                showView();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        if (!key.isKeyDown() && drawOverlay) {
            hideView();
        }

        if (drawOverlay) {
            gui.draw();
        }
    }

    private void showView() {
        boolean canOpen = updateGuiData();
        if (canOpen) {
            mc.inGameHasFocus = false;
            drawOverlay = true;
            mc.mouseHelper.ungrabMouseCursor();
        }
    }

    private void hideView() {
        mc.inGameHasFocus = true;
        drawOverlay = false;
        mc.mouseHelper.grabMouseCursor();

        int focusIndex = findIndex();
        if (focusIndex != -1) {
            equipToolbeltItem(focusIndex);
        }
    }

    private void equipToolbeltItem(int toolbeltItemIndex) {
        EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(toolbeltItemIndex);
        //packet.handleClientSide(mc.thePlayer);
        PacketPipeline.instance.sendToServer(packet);
    }

    private boolean updateGuiData() {
        InventoryToolbelt inventoryToolbelt = UtilToolbelt.findToolbeltInventory(mc.thePlayer);
        if (inventoryToolbelt != null) {
            gui.setInventoryToolbelt(inventoryToolbelt);
            return true;
        }

        return false;
    }

    private int findIndex() {
        return gui.getFocus();
    }
}
