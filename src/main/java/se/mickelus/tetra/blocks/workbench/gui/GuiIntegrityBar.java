package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.gui.GuiStringSmall;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;

public class GuiIntegrityBar extends GuiElement {

    private static final int segmentWidth = 8;
    private static final int segmentHeight = 2;
    private static final int segmentOffset = 6;

    private static final int gainColor = 0x22ffffff;
    private static final int costColor = 0xffffffff;
    private static final int overuseColor = 0x88ff5555;

    private static final String labelText = "%sIntegrity usage: %d/%d";

    private int integrityGain;
    private int integrityCost;

    private GuiString label;

    public GuiIntegrityBar(int x, int y) {
        super(x, y, 0, 0);

        label = new GuiStringSmall(0, 0, "", GuiAlignment.center);
        addChild(label);
    }

    public void setItemStack(ItemStack itemStack, ItemStack previewStack) {
        boolean shouldShow = !itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular;
        setVisible(shouldShow);
        if (shouldShow) {
            if (!previewStack.isEmpty()) {
                integrityGain = ItemModular.getIntegrityGain(previewStack);
                integrityCost = ItemModular.getIntegrityCost(previewStack);
            } else {
                integrityGain = ItemModular.getIntegrityGain(itemStack);
                integrityCost = ItemModular.getIntegrityCost(itemStack);
            }

            label.setString(String.format(labelText,
                    integrityGain + integrityCost < 0 ? "§c" : "", -integrityCost, integrityGain));
        }
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        int offset = integrityGain * ( segmentWidth + 1) / 2;

        for (int i = 0; i < -integrityCost; i++) {
            drawSegment(refX + x  - offset + i * (segmentWidth + 1),refY + y + segmentOffset,
                    i >= integrityGain ? overuseColor : costColor);
        }

        for (int i = -integrityCost; i < integrityGain; i++) {
            drawSegment(refX + x  - offset + i * (segmentWidth + 1),refY + y + segmentOffset, gainColor);
        }
    }

    private void drawSegment(int x, int y, int color) {
        drawRect(x, y,x + segmentWidth, y + segmentHeight, color);
    }

}
