package se.mickelus.tetra.items.modular.impl.toolbelt;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.OverlayGuiToolbelt;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OverlayToolbelt {

    public static final String bindingGroup = "tetra.toolbelt.binding.group";
    public static OverlayToolbelt instance;
    private final Minecraft mc;
    private final OverlayGuiToolbelt gui;
    public KeyMapping accessBinding;
    public KeyMapping restockBinding;
    public KeyMapping openBinding;
    private long openTime = -1;
    // due to gui visibility tricks, let's use this to keep track of when we should show or hide the gui
    private boolean isActive = false;

    public OverlayToolbelt(Minecraft mc) {
        this.mc = mc;

        gui = new OverlayGuiToolbelt(mc);

        accessBinding = new KeyMapping("tetra.toolbelt.binding.access", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B, bindingGroup);
        restockBinding = new KeyMapping("tetra.toolbelt.binding.restock", KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);
        openBinding = new KeyMapping("tetra.toolbelt.binding.open", KeyConflictContext.IN_GAME, KeyModifier.ALT,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);

        ClientRegistry.registerKeyBinding(accessBinding);
        ClientRegistry.registerKeyBinding(restockBinding);
        ClientRegistry.registerKeyBinding(openBinding);

        instance = this;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (restockBinding.isDown()) {
            equipToolbeltItem(ToolbeltSlotType.quickslot, -1, InteractionHand.OFF_HAND);
        } else if (openBinding.isDown()) {
            openToolbelt();
        } else if (accessBinding.isDown() && mc.isWindowActive() && !isActive) {
            showView();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
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

    private void equipToolbeltItem(ToolbeltSlotType slotType, int toolbeltItemIndex, InteractionHand hand) {
        EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(slotType, toolbeltItemIndex, hand);
        TetraMod.packetHandler.sendToServer(packet);
        if (toolbeltItemIndex > -1) {
            ToolbeltHelper.equipItemFromToolbelt(mc.player, slotType, toolbeltItemIndex, hand);
        } else {
            boolean storeItemSuccess = ToolbeltHelper.storeItemInToolbelt(mc.player);
            if (!storeItemSuccess) {
                mc.player.displayClientMessage(new TranslatableComponent("tetra.toolbelt.full"), true);
            }
        }
    }

    private void quickEquip() {
        if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult raytrace = (BlockHitResult) mc.hitResult;
            BlockState blockState = mc.level.getBlockState(raytrace.getBlockPos());
            int index = ToolbeltHelper.getQuickAccessSlotIndex(mc.player, mc.hitResult, blockState);

            if (index > -1) {
                equipToolbeltItem(ToolbeltSlotType.quickslot, index, InteractionHand.MAIN_HAND);
            }
        }
    }

    /**
     * Requests the server to open the toolbelt container UI
     *
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

    private InteractionHand getHand() {
        return gui.getFocusHand();
    }

    private ToolbeltSlotType findSlotType() {
        return gui.getFocusType();
    }
}
