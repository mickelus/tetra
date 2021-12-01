package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;

import java.util.Arrays;
import java.util.Objects;

public class OverlayGuiQuiverGroup extends GuiElement {
    private OverlayGuiQuiverSlot[] slots = new OverlayGuiQuiverSlot[0];
    QuiverInventory inventory;

    public OverlayGuiQuiverGroup(int x, int y) {
        super(x, y, 0, 0);
        setAttachmentPoint(GuiAttachment.bottomRight);
    }

    public void setInventory(QuiverInventory inventory) {
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

    public InteractionHand getHand() {
        return null;
    }
}
