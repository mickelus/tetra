package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import se.mickelus.mgui.gui.GuiRoot;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.PotionsInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;

public class OverlayGuiToolbelt extends GuiRoot {

    private OverlayGuiQuickslotGroup quickslotGroup;
    private OverlayGuiPotionGroup potionGroup;
    private OverlayGuiQuiverGroup quiverGroup;

    public OverlayGuiToolbelt(Minecraft mc) {
        super(mc);

        quickslotGroup = new OverlayGuiQuickslotGroup(42, 0);
        addChild(quickslotGroup);

        potionGroup = new OverlayGuiPotionGroup(0, 30);
        addChild(potionGroup);

        quiverGroup = new OverlayGuiQuiverGroup(-30, -30);
        addChild(quiverGroup);

    }

    public void setInventories(ItemStack itemStack) {
        quickslotGroup.setInventory(new QuickslotInventory(itemStack));
        potionGroup.setInventory(new PotionsInventory(itemStack));
        quiverGroup.setInventory(new QuiverInventory(itemStack));
    }

    public void setVisible(boolean visible) {
        if (visible) {
            quickslotGroup.setVisible(true);
            potionGroup.setVisible(true);
            quiverGroup.setVisible(true);
        } else {
            quickslotGroup.setVisible(false);
            potionGroup.setVisible(false);
            quiverGroup.setVisible(false);
        }
    }

    @Override
    public void draw() {
        if (isVisible()) {
            MainWindow window = mc.getMainWindow();
            int width = window.getScaledWidth();
            int height = window.getScaledHeight();

            int mouseX = (int)(mc.mouseHelper.getMouseX() * window.getScaledWidth() / window.getWidth());
            int mouseY = (int)(mc.mouseHelper.getMouseY() * window.getScaledHeight() / window.getHeight());

            this.drawChildren(new MatrixStack(), width / 2, height / 2, 0, 0, mouseX, mouseY, 1.0F);
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

    public Hand getFocusHand() {
        Hand quickslotHand = quickslotGroup.getHand();
        if (quickslotHand != null) {
            return quickslotHand;
        }

        Hand potionHand = potionGroup.getHand();
        if (potionHand != null) {
            return potionHand;
        }

        Hand quiverHand = quiverGroup.getHand();
        if (quiverHand != null) {
            return quiverHand;
        }

        return Hand.OFF_HAND;
    }
}
