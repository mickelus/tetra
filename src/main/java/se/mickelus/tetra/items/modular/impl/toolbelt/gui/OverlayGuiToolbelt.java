package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.PotionsInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OverlayGuiToolbelt extends GuiRoot {

    private final OverlayGuiQuickslotGroup quickslotGroup;
    private final OverlayGuiPotionGroup potionGroup;
    private final OverlayGuiQuiverGroup quiverGroup;

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
            Window window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();

            int mouseX = (int) (mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());

            this.drawChildren(new PoseStack(), width / 2, height / 2, 0, 0, mouseX, mouseY, 1.0F);
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
        return quiverFocus;
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
