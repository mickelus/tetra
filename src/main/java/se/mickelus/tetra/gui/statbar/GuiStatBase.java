package se.mickelus.tetra.gui.statbar;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;

public abstract class GuiStatBase extends GuiElement {
    public GuiStatBase(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public abstract void update(ItemStack currentStack, ItemStack previewStack, String slot, String improvement);

    public abstract boolean shouldShow(ItemStack currentStack, ItemStack previewStack, String slot, String improvement);
}
