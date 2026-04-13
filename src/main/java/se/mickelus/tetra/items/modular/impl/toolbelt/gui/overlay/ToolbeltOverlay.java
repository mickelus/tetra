package se.mickelus.tetra.items.modular.impl.toolbelt.gui.overlay;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.client.keymap.TetraKeyMappings;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.EquipToolbeltItemPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.OpenToolbeltItemPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.StoreToolbeltItemPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.PotionsInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ToolbeltOverlay extends GuiRoot implements LayeredDraw.Layer {

    private final QuickslotGroupGui quickslotGroup;
    private final PotionGroupGui potionGroup;
    private final QuiverGroupGui quiverGroup;
    private final HolosphereGroupGui holosphereGroup;

    private long openTime = -1;
    // due to gui visibility tricks, let's use this to keep track of when we should show or hide the gui
    private boolean isActive = false;

    public ToolbeltOverlay(Minecraft mc) {
        super(mc);

        quickslotGroup = new QuickslotGroupGui(52, 0);
        quickslotGroup.setAttachmentAnchor(GuiAttachment.middleCenter);
        addChild(quickslotGroup);

        potionGroup = new PotionGroupGui(0, 40);
        potionGroup.setAttachmentPoint(GuiAttachment.topCenter);
        potionGroup.setAttachmentAnchor(GuiAttachment.middleCenter);
        addChild(potionGroup);

        quiverGroup = new QuiverGroupGui(-40, -40);
        quiverGroup.setAttachmentPoint(GuiAttachment.bottomRight);
        quiverGroup.setAttachmentAnchor(GuiAttachment.middleCenter);
        addChild(quiverGroup);

        holosphereGroup = new HolosphereGroupGui(0, -40);
        holosphereGroup.setAttachmentAnchor(GuiAttachment.middleCenter);
        addChild(holosphereGroup);
    }

    public void setInventories(ItemStack itemStack) {
    }


    public void toggleActive(boolean active) {
        this.isActive = active;

        quickslotGroup.setVisible(active);
        potionGroup.setVisible(active);
        quiverGroup.setVisible(active);
        holosphereGroup.setVisible(active);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (TetraKeyMappings.restockBinding.isDown()) {
            storeToolbeltItem();
        } else if (TetraKeyMappings.openBinding.isDown() && TetraKeyMappings.openBinding.consumeClick()) {
            openToolbelt();
        } else if (TetraKeyMappings.accessBinding.isDown() && mc.isWindowActive() && !isActive) {
            showView();
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!TetraKeyMappings.accessBinding.isDown() && isActive) {
            hideView();
        }

        this.draw(graphics);
    }

    @Override
    public void draw(GuiGraphics graphics) {
        super.draw(graphics);
        if (isVisible()) {
            Window window = mc.getWindow();
            int mouseX = (int) (mc.mouseHandler.xpos() * this.width / window.getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * this.height / window.getScreenHeight());
            this.updateFocusState(0, 0, mouseX, mouseY);
        }
    }

    private void showView() {
        boolean canOpen = updateGuiData();
        if (canOpen) {
            toggleActive(true);
            mc.mouseHandler.releaseMouse();
            openTime = System.currentTimeMillis();
        }
    }

    private void hideView() {
        toggleActive(false);
        mc.mouseHandler.grabMouse();

        int focusIndex = getFocusIndex();
        if (focusIndex != -1) {
            equipToolbeltItem(getFocusType(), focusIndex, getFocusHand());
        } else if (System.currentTimeMillis() - openTime < 500) {
            quickEquip();
        }

        holosphereGroup.performActions();
    }


    private boolean openToolbelt() {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(mc.player);
        if (!itemStack.isEmpty()) {
            TetraMod.packetHandler.sendToServer(new OpenToolbeltItemPacket());
        }

        return !itemStack.isEmpty();
    }

    private boolean updateGuiData() {
        boolean canShow = false;

        ItemStack toolbeltStack = ToolbeltHelper.findToolbelt(mc.player);
        if (!toolbeltStack.isEmpty()) {
            quickslotGroup.setInventory(new QuickslotInventory(toolbeltStack, mc.player.registryAccess()));
            potionGroup.setInventory(new PotionsInventory(toolbeltStack, mc.player.registryAccess()));
            quiverGroup.setInventory(new QuiverInventory(toolbeltStack, mc.player.registryAccess()));

            canShow = true;
        } else {
            quickslotGroup.clear();
            potionGroup.clear();
            quiverGroup.clear();
        }

        ItemStack holosphereStack = ModularHolosphereItem.findHolosphere(mc.player);
        if (!holosphereStack.isEmpty()) {
            holosphereGroup.update(holosphereStack);

            canShow = true;
        } else {
            holosphereGroup.clear();
        }

        return canShow;
    }

    private void equipToolbeltItem(ToolbeltSlotType slotType, int toolbeltItemIndex, InteractionHand hand) {
        if (toolbeltItemIndex > -1) {
            EquipToolbeltItemPacket packet = new EquipToolbeltItemPacket(slotType, toolbeltItemIndex, hand);
            TetraMod.packetHandler.sendToServer(packet);

            ToolbeltHelper.equipItemFromToolbelt(mc.player, slotType, toolbeltItemIndex, hand);
        }
    }

    private void storeToolbeltItem() {
        boolean storeItemSuccess = ToolbeltHelper.storeItemInToolbelt(mc.player);
        StoreToolbeltItemPacket packet = new StoreToolbeltItemPacket();
        TetraMod.packetHandler.sendToServer(packet);

        if (!storeItemSuccess) {
            mc.player.displayClientMessage(Component.translatable("tetra.toolbelt.full"), true);
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

    public ToolbeltSlotType getFocusType() {
        if (quickslotGroup.getFocus() != -1) {
            return ToolbeltSlotType.quickslot;
        }

        if (potionGroup.getFocus() != -1) {
            return ToolbeltSlotType.potion;
        }

        if (quiverGroup.getFocus() != -1) {
            return ToolbeltSlotType.quiver;
        }

        return ToolbeltSlotType.quickslot;
    }

    public int getFocusIndex() {
        int quickslotFocus = quickslotGroup.getFocus();
        if (quickslotFocus != -1) {
            return quickslotFocus;
        }

        int potionFocus = potionGroup.getFocus();
        if (potionFocus != -1) {
            return potionFocus;
        }

        int quiverFocus = quiverGroup.getFocus();
        if (quiverFocus != -1) {
            return quiverFocus;
        }

        return -1;
    }

    public InteractionHand getFocusHand() {
        InteractionHand quickslotHand = quickslotGroup.getHand();
        if (quickslotHand != null) {
            return quickslotHand;
        }

        InteractionHand potionHand = potionGroup.getHand();
        if (potionHand != null) {
            return potionHand;
        }

        InteractionHand quiverHand = quiverGroup.getHand();
        if (quiverHand != null) {
            return quiverHand;
        }

        return InteractionHand.OFF_HAND;
    }
}
