package se.mickelus.tetra.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiItem;

import java.util.Arrays;
import java.util.List;

public class GuiItemRolling extends GuiElement {
    private boolean showTooltip = true;

    private GuiItem[] items = new GuiItem[0];

    public GuiItemRolling(int x, int y) {
        super(x, y, 16, 16);
        this.setVisible(false);
    }

    public GuiItemRolling setTooltip(boolean showTooltip) {
        this.showTooltip = showTooltip;
        return this;
    }

    public GuiItemRolling setItems(ItemStack[] itemStacks) {
        items = Arrays.stream(itemStacks)
                .map(itemStack -> new GuiItem(0, 0).setItem(itemStack).setCount(false))
                .toArray(GuiItem[]::new);

        return this;
    }

    @Override
    protected void drawChildren(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (items.length > 0) {
            int offset = (int) (System.currentTimeMillis() / 1000) % items.length;
            items[offset].draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }

    public List<String> getTooltipLines() {
        if (showTooltip && items.length > 0) {
            int offset = (int) (System.currentTimeMillis() / 1000) % items.length;
            return items[offset].getTooltipLines();
        }

        return null;
    }
}
