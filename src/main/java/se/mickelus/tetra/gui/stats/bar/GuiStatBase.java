package se.mickelus.tetra.gui.stats.bar;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.mutil.gui.GuiElement;

public abstract class GuiStatBase extends GuiElement {
    public GuiStatBase(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public abstract void update(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement);

    public abstract boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement);

    public abstract void setAlignment(GuiAlignment alignment);
}
