package se.mickelus.tetra.items.toolbelt;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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
import se.mickelus.tetra.items.toolbelt.gui.OverlayGuiToolbelt;
import se.mickelus.tetra.items.toolbelt.inventory.ToolbeltSlotType;
import se.mickelus.tetra.network.PacketHandler;

public class OverlayToolbelt {

    public static OverlayToolbelt instance;

    private final Minecraft mc;

    public static final String bindingGroup = "toolbelt.binding.group";

    public KeyBinding accessBinding;
    public KeyBinding restockBinding;

    private long openTime = -1;

    // due to gui visibility tricks, let's use this to keep track of when we should show or hide the gui
    private boolean isActive = false;

    private OverlayGuiToolbelt gui;

    public OverlayToolbelt(Minecraft mc) {
        this.mc = mc;

        gui = new OverlayGuiToolbelt(mc);

        accessBinding = new KeyBinding("toolbelt.binding.access", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_V, bindingGroup);
        restockBinding = new KeyBinding("toolbelt.binding.restock", KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
                InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_V, bindingGroup);

        ClientRegistry.registerKeyBinding(accessBinding);
        ClientRegistry.registerKeyBinding(restockBinding);

        instance = this;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (restockBinding.isKeyDown()) {
            equipToolbeltItem(ToolbeltSlotType.quickslot, -1, Hand.OFF_HAND);
        } else if (accessBinding.isKeyDown() && mc.isGameFocused() && !isActive) {
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
            // todo 1.14: this changed, check if overlay still works
            mc.mouseHelper.ungrabMouse();
            isActive = true;
            openTime = System.currentTimeMillis();
        }
    }

    private void hideView() {
        // todo 1.14: this changed, check if overlay still works
        gui.setVisible(false);
        mc.mouseHelper.grabMouse();
        isActive = false;

        int focusIndex = findIndex();
        if (focusIndex != -1) {
            equipToolbeltItem(findSlotType(), focusIndex, Hand.OFF_HAND);
        } else if (System.currentTimeMillis() - openTime < 500) {
            quickEquip();
        }
    }

    private void equipToolbeltItem(ToolbeltSlotType slotType, int toolbeltItemIndex, Hand hand) {
        EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(slotType, toolbeltItemIndex, hand);
        PacketHandler.sendToServer(packet);
        if (toolbeltItemIndex > -1) {
            ToolbeltHelper.equipItemFromToolbelt(mc.player, slotType, toolbeltItemIndex, hand);
        } else {
            boolean storeItemSuccess = ToolbeltHelper.storeItemInToolbelt(mc.player);
            if (!storeItemSuccess) {
                mc.player.sendStatusMessage(new TranslationTextComponent("toolbelt.full"), true);
            }
        }
    }

    private void quickEquip() {
        if (mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
            // todo 1.14: this changed, check if quick equip still works
            BlockState blockState = mc.world.getBlockState(new BlockPos(mc.objectMouseOver.getHitVec()));
            int index = ToolbeltHelper.getQuickAccessSlotIndex(mc.player, mc.objectMouseOver, blockState);

            if (index > -1) {
                equipToolbeltItem(ToolbeltSlotType.quickslot, index, Hand.MAIN_HAND);
            }
        }
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

    private ToolbeltSlotType findSlotType() {
        return gui.getFocusType();
    }
}
