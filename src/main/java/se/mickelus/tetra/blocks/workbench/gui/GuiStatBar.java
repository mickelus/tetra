package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.gui.GuiStringSmall;

public class GuiStatBar extends GuiElement {

    private static final int barMaxLength = 98;
    private static final int barHeight = 1;

    private static final String increaseColorFont = "§a";
    private static final String decreaseColorFont = "§c";

    private static final int increaseColorBar = 0x8855ff55;
    private static final int decreaseColorBar = 0x88ff5555;

    private double min;
    private double max;

    private double value;
    private double diffValue;

    private int barLength;
    private int diffLength;

    private GuiString valueLabel;

    private GuiAlignment alignment;

    public GuiStatBar(int x, int y, String label, double min, double max, GuiAlignment alignment) {
        super(x, y, 98, 16);

        this.min = min;
        this.max = max;
        this.alignment = alignment;

        if (alignment == GuiAlignment.right) {
            addChild(new GuiStringSmall(barMaxLength, 0, label, alignment));
            valueLabel = new GuiString(-4, 3, label, alignment);
        } else {
            addChild(new GuiStringSmall(0, 0, label, alignment));
            valueLabel = new GuiString(barMaxLength + 4, 3, label, alignment);
        }
        addChild(valueLabel);
    }

    public void setValue(double value, double diffValue) {
        this.value = value;
        this.diffValue = diffValue;

        calculateBarLengths();
    }

    private void calculateBarLengths() {
        double minValue = Math.min(value, diffValue);

        barLength = (int) Math.floor((minValue - min) / (max - min) * barMaxLength);
        diffLength = (int) Math.ceil( Math.abs(value - diffValue) / (max - min) * barMaxLength);

        if (value != diffValue) {
            if (alignment == GuiAlignment.right) {
                valueLabel.setString(String.format("%s(%+.02f) §r%.02f",
                        value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value, diffValue));
            } else {
                valueLabel.setString(String.format("%.02f %s(%+.02f)",
                        diffValue, value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value));
            }
        } else {
            valueLabel.setString(String.format("%.02f", diffValue));
        }

    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        drawRect(refX + x, refY + y + 6,refX + x + barMaxLength, refY + y + 6 + barHeight, 0x22ffffff);
        if (alignment == GuiAlignment.right) {
            drawRect(refX + x + barLength, refY + y + 6,refX + x + barMaxLength, refY + y + 6 + barHeight, 0xffffffff);
            drawRect(refX + x + barLength - diffLength, refY + y + 6,refX + x + barLength, refY + y + 6 + barHeight,
                    value < diffValue ? increaseColorBar : decreaseColorBar);
        } else {
            drawRect(refX + x, refY + y + 6,refX + x + barLength, refY + y + 6 + barHeight, 0xffffffff);
            drawRect(refX + x + barLength, refY + y + 6,refX + x + barLength + diffLength, refY + y + 6 + barHeight,
                    value < diffValue ? increaseColorBar : decreaseColorBar);
        }
    }
}
