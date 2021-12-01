package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.OverlayGuiToolbelt;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;

public class OverlayToolbelt {

    public static OverlayToolbelt instance;

    private final Minecraft mc;

    public static final String bindingGroup = "tetra.toolbelt.binding.group";

    public KeyBinding accessBinding;
    public KeyBinding restockBinding;
    public KeyBinding openBinding;

    private long openTime = -1;

    // due to gui visibility tricks, let's use this to keep track of when we should show or hide the gui
    private boolean isActive = false;

    private OverlayGuiToolbelt gui;

    public OverlayToolbelt(Minecraft mc) {
        this.mc = mc;

        gui = new OverlayGuiToolbelt(mc);

        accessBinding = new KeyBinding("tetra.toolbelt.binding.access", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_B, bindingGroup);
        restockBinding = new KeyBinding("tetra.toolbelt.binding.restock", KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
                InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);
        openBinding = new KeyBinding("tetra.toolbelt.binding.open", KeyConflictContext.IN_GAME, KeyModifier.ALT,
                InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);

        ClientRegistry.registerKeyBinding(accessBinding);
        ClientRegistry.registerKeyBinding(restockBinding);
        ClientRegistry.registerKeyBinding(openBinding);

        instance = this;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (restockBinding.isDown()) {
            equipToolbeltItem(ToolbeltSlotType.quickslot, -1, Hand.OFF_HAND);
        } else if (openBinding.isDown()) {
            openToolbelt();
        } else if (accessBinding.isDown() && mc.isWindowActive() && !isActive) {
            showView();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        if (!accessBinding.isDown() && isActive) {
            hideView();
        }

        gui.draw();
    }

    private void showView() {
        boolean canOpen = updateGuiData();
        if (canOpen) {
            mc.mouseHandler.releaseMouse();
            isActive = true;
            openTime = System.currentTimeMillis();
        }
    }

    private void hideView() {
        gui.setVisible(false);
        mc.mouseHandler.grabMouse();
        isActive = false;

        int focusIndex = findIndex();
        if (focusIndex != -1) {
            equipToolbeltItem(findSlotType(), focusIndex, getHand());
        } else if (System.currentTimeMillis() - openTime < 500) {
            quickEquip();
        }
    }

    private void equipToolbeltItem(ToolbeltSlotType slotType, int toolbeltItemIndex, Hand hand) {
        EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(slotType, toolbeltItemIndex, hand);
        TetraMod.packetHandler.sendToServer(packet);
        if (toolbeltItemIndex > -1) {
            ToolbeltHelper.equipItemFromToolbelt(mc.player, slotType, toolbeltItemIndex, hand);
        } else {
            boolean storeItemSuccess = ToolbeltHelper.storeItemInToolbelt(mc.player);
            if (!storeItemSuccess) {
                mc.player.displayClientMessage(new TranslationTextComponent("tetra.toolbelt.full"), true);
            }
        }
    }

    private void quickEquip() {
        if (mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult raytrace = (BlockRayTraceResult) mc.hitResult;
            BlockState blockState = mc.level.getBlockState(raytrace.getBlockPos());
            int index = ToolbeltHelper.getQuickAccessSlotIndex(mc.player, mc.hitResult, blockState);

            if (index > -1) {
                equipToolbeltItem(ToolbeltSlotType.quickslot, index, Hand.MAIN_HAND);
            }
        }
    }

    /**
     * Requests the server to open the toolbelt container UI
     * @return true if the player has a toolbelt
     */
    private boolean openToolbelt() {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(mc.player);
        if (!itemStack.isEmpty()) {
            TetraMod.packetHandler.sendToServer(new OpenToolbeltItemPacket());
        }

        return !itemStack.isEmpty();
    }

    private boolean updateGuiData() {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(mc.player);
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

    private Hand getHand() {
        return gui.getFocusHand();
    }

    private ToolbeltSlotType findSlotType() {
        return gui.getFocusType();
    }
}
