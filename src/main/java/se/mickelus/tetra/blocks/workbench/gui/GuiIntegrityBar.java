package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;

import java.util.Collections;
import java.util.List;

public class GuiIntegrityBar extends GuiElement {

    private static final int segmentWidth = 8;
    private static final int segmentHeight = 2;
    private static final int segmentOffset = 6;

    private static final int gainColor = 0x22ffffff;
    private static final int costColor = 0xffffffff;
    private static final int overuseColor = 0x88ff5555;

    private int integrityGain;
    private int integrityCost;

    private GuiString label;

    private List<String> tooltip;

    public GuiIntegrityBar(int x, int y) {
        super(x, y, 0, 8);

        label = new GuiStringSmall(0, 0, "");
        label.setAttachment(GuiAttachment.topCenter);
        addChild(label);

        setAttachmentPoint(GuiAttachment.topCenter);

        tooltip = Collections.singletonList(I18n.format("stats.integrity_usage.tooltip"));
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

            if (integrityGain + integrityCost < 0) {
                label.setString(TextFormatting.RED + I18n.format("stats.integrity_usage", -integrityCost, integrityGain));
            } else {
                label.setString(I18n.format("stats.integrity_usage", -integrityCost, integrityGain));
            }

            width = integrityGain * ( segmentWidth + 1);
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        for (int i = 0; i < -integrityCost; i++) {
            drawSegment(refX + x + i * (segmentWidth + 1),refY + y + segmentOffset,
                    i >= integrityGain ? overuseColor : costColor);
        }

        for (int i = -integrityCost; i < integrityGain; i++) {
            drawSegment(refX + x + i * (segmentWidth + 1),refY + y + segmentOffset, gainColor);
        }
    }

    private void drawSegment(int x, int y, int color) {
        drawRect(x, y,x + segmentWidth, y + segmentHeight, color);
    }

}
