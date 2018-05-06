package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import se.mickelus.tetra.items.toolbelt.gui.OverlayGuiToolbelt;
import se.mickelus.tetra.items.toolbelt.inventory.ToolbeltSlotType;
import se.mickelus.tetra.network.PacketHandler;

public class OverlayToolbelt {

    public static OverlayToolbelt instance;

    private final Minecraft mc;

    public static final String bindingGroup = "toolbelt.binding.group";

    public KeyBinding accessBinding;
    public KeyBinding restockBinding;

    // due to gui visibility tricks, let's use this to keep track of when we should show or hide the gui
    private boolean isActive = false;

    private OverlayGuiToolbelt gui;

    public OverlayToolbelt(Minecraft mc) {
        this.mc = mc;

        gui = new OverlayGuiToolbelt(mc);

        accessBinding = new KeyBinding("toolbelt.binding.access", KeyConflictContext.IN_GAME,
                Keyboard.KEY_V, bindingGroup);
        restockBinding = new KeyBinding("toolbelt.binding.restock", KeyConflictContext.IN_GAME,
                KeyModifier.SHIFT, Keyboard.KEY_V, bindingGroup);

        ClientRegistry.registerKeyBinding(accessBinding);
        ClientRegistry.registerKeyBinding(restockBinding);

        instance = this;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (restockBinding.isPressed()) {
            equipToolbeltItem(ToolbeltSlotType.quickslot, -1);
        } else if (accessBinding.isPressed() && mc.inGameHasFocus) {
            showView();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        if (!accessBinding.isKeyDown() && isActive) {
            hideView();
        }

        gui.draw();
    }

    private void showView() {
        boolean canOpen = updateGuiData();
        if (canOpen) {
            mc.inGameHasFocus = false;
            mc.mouseHelper.ungrabMouseCursor();
            isActive = true;
        }
    }

    private void hideView() {
        mc.inGameHasFocus = true;
        gui.setVisible(false);
        mc.mouseHelper.grabMouseCursor();
        isActive = false;

        int focusIndex = findIndex();
        if (focusIndex != -1) {
            equipToolbeltItem(findSlotType(), focusIndex);
        }
    }

    private void equipToolbeltItem(ToolbeltSlotType slotType, int toolbeltItemIndex) {
        EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(slotType, toolbeltItemIndex);
        PacketHandler.sendToServer(packet);
        if (toolbeltItemIndex > -1) {
            UtilToolbelt.equipItemFromToolbelt(mc.player, slotType, toolbeltItemIndex, EnumHand.OFF_HAND);
        } else {
            boolean storeItemSuccess = UtilToolbelt.storeItemInToolbelt(mc.player);
            if (!storeItemSuccess) {
                mc.player.sendStatusMessage(new TextComponentTranslation("toolbelt.full"), true);
            }
        }
    }

    private boolean updateGuiData() {
        ItemStack itemStack = UtilToolbelt.findToolbelt(mc.player);
        if (!itemStack.isEmpty()) {
            gui.setInventories(itemStack);
            gui.setVisible(true);
            return true;
        }

        return false;
    }

    private int findIndex() {
        return gui.getFocusIndex();
    }

    private ToolbeltSlotType findSlotType() {
        return gui.getFocusType();
    }
}
