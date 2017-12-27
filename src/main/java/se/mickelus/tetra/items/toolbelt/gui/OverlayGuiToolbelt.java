package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiRect;
import se.mickelus.tetra.gui.GuiRoot;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.items.toolbelt.InventoryToolbelt;

public class OverlayGuiToolbelt extends GuiRoot {

    private static final ResourceLocation toolbeltTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");

    private OverlayGuiItem[] items = new OverlayGuiItem[0];

    public OverlayGuiToolbelt(Minecraft mc) {
        super(mc);
    }

    public void setInventoryToolbelt(InventoryToolbelt inventoryToolbelt) {
        clearChildren();
        int numSlots = inventoryToolbelt.getSizeInventory();
        items = new OverlayGuiItem[numSlots];

        addChild(new GuiTexture(47, numSlots * -12 - 7, 22, 7, 0, 28, toolbeltTexture));
        addChild(new GuiTexture(47, numSlots * 12, 22, 7, 0, 35, toolbeltTexture));
        addChild(new GuiRect(47, numSlots * -12, 22, numSlots * 24, 0xcc000000));

        for (int i = 0; i < numSlots; i++) {
            ItemStack itemStack = inventoryToolbelt.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                items[i] = new OverlayGuiItem(10, numSlots * -12 + i * 24 + 3, itemStack, i);
                addChild(items[i]);
            }
        }
    }

    @Override
    public void draw() {
        if (isVisible()) {
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
            drawChildren(width/2, height/2, width, height, mouseX, mouseY);
        }
    }

    public int getFocus() {
        for (int i = 0; i < items.length; i++) {
            OverlayGuiItem element = items[i];
            if (items[i] != null && element.hasFocus()) {
                return element.getSlot();
            }
        }
        return -1;
    }
}
