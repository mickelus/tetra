package se.mickelus.tetra.gui.impl.statbar;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiElement;

public abstract class GuiStatBase extends GuiElement {
    public GuiStatBase(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public abstract void update(EntityPlayer player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement);

    public abstract boolean shouldShow(EntityPlayer player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement);

    public abstract void setAlignment(GuiAlignment alignment);
}
