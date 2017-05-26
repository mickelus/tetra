package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import se.mickelus.tetra.gui.GuiRoot;

public class OverlayGuiToolbelt extends GuiRoot {

    public OverlayGuiToolbelt(Minecraft mc) {
        super(mc);
    }

    public void setInventoryToolbelt(InventoryToolbelt inventoryToolbelt) {
        clearChildren();
        for (int i = 0; i < inventoryToolbelt.getSizeInventory(); i++) {
            ItemStack itemStack = inventoryToolbelt.getStackInSlot(i);
            if (!itemStack.func_190926_b()) {
                addChild(new OverlayGuiItem(i * 25,  i * 25 - 100, itemStack, i));
            }
        }
    }

    @Override
    public void draw() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
        drawChildren(width/2, height/2, width, height, mouseX, mouseY);
    }

    public int getFocus() {
        for (int i = 0; i < getNumChildren(); i++) {
            OverlayGuiItem element = (OverlayGuiItem) getChild(i);
            if (element.hasFocus()) {
                return element.getSlot();
            }
        }
        return -1;
    }
}
