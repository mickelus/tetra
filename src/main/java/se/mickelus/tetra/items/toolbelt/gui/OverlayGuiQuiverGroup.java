package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuiver;

import java.util.Arrays;
import java.util.Objects;

public class OverlayGuiQuiverGroup extends GuiElement {
    private OverlayGuiQuiverSlot[] slots = new OverlayGuiQuiverSlot[0];
    InventoryQuiver inventory;

    public OverlayGuiQuiverGroup(int x, int y) {
        super(x, y, 0, 0);
        setAttachmentPoint(GuiAttachment.bottomRight);
    }

    public void setInventory(InventoryQuiver inventory) {
        clearChildren();
        this.inventory = inventory;
        ItemStack[] aggregatedStacks = inventory.getAggregatedStacks();
        slots = new OverlayGuiQuiverSlot[aggregatedStacks.length];

        width = aggregatedStacks.length * 13;
        height = aggregatedStacks.length * 13;

        for (int i = 0; i < aggregatedStacks.length; i++) {
            ItemStack itemStack = aggregatedStacks[i];
            slots[i] = new OverlayGuiQuiverSlot(-13 * i, -13 * i, itemStack, i);
            addChild(slots[i]);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            Arrays.stream(slots)
                    .filter(Objects::nonNull)
                    .forEach(item -> item.setVisible(true));
        } else {
            Arrays.stream(slots)
                    .filter(Objects::nonNull)
                    .forEach(item -> item.setVisible(false));
        }
    }

    public int getFocus() {
        for (int i = 0; i < slots.length; i++) {
            OverlayGuiQuiverSlot element = slots[i];
            if (element != null && element.hasFocus()) {
                ItemStack itemStack = element.getItemStack();
                return inventory.getFirstIndexForStack(itemStack);
            }
        }
        return -1;
    }
}
