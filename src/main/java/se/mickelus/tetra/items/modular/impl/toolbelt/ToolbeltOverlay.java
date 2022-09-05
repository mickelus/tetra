package se.mickelus.tetra.items.modular.impl.toolbelt;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.OverlayGuiToolbelt;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ToolbeltOverlay implements IGuiOverlay {
    private final Minecraft mc;
    private final OverlayGuiToolbelt gui;
    private long openTime = -1;
    // due to gui visibility tricks, let's use this to keep track of when we should show or hide the gui
    private boolean isActive = false;

    public ToolbeltOverlay(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiToolbelt(mc);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (ToolbeltKeyMappings.restockBinding.isDown()) {
            equipToolbeltItem(ToolbeltSlotType.quickslot, -1, InteractionHand.OFF_HAND);
        } else if (ToolbeltKeyMappings.openBinding.isDown()) {
            openToolbelt();
        } else if (ToolbeltKeyMappings.accessBinding.isDown() && mc.isWindowActive() && !isActive) {
            showView();
        }
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!ToolbeltKeyMappings.accessBinding.isDown() && isActive) {
            hideView();
        }

        this.gui.draw();
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
                mc.player.displayClientMessage(Component.translatable("tetra.toolbelt.full"), true);
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
