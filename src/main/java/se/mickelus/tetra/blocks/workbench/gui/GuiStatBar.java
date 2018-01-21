package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.gui.GuiStringSmall;

public class GuiStatBar extends GuiElement {

    protected static final int barMaxLength = 98;
    protected static final int barHeight = 1;

    protected static final String increaseColorFont = "§a";
    protected static final String decreaseColorFont = "§c";

    protected static final int increaseColorBar = 0x8855ff55;
    protected static final int decreaseColorBar = 0x88ff5555;
    protected int diffColor;

    protected double min;
    protected double max;

    protected double value;
    protected double diffValue;

    protected int barLength;
    protected int diffLength;

    protected GuiString labelString;
    protected GuiString valueString;

    protected GuiAlignment alignment;

    public GuiStatBar(int x, int y, String label, double min, double max, GuiAlignment alignment) {
        super(x, y, 98, 16);

        this.min = min;
        this.max = max;
        this.alignment = alignment;

        if (alignment == GuiAlignment.right) {
            labelString = new GuiStringSmall(0, 0, label, alignment);
            valueString = new GuiString(0, 3, label, alignment);
        } else {
            labelString = new GuiStringSmall(0, 0, label, alignment);
            valueString = new GuiString(0, 3, label, alignment);
        }

        addChild(labelString);
        addChild(valueString);
    }

    public void setAlignment(GuiAlignment alignment) {
        this.alignment = alignment;
        realign();
    }

    private void realign() {
        if (alignment == GuiAlignment.right) {
            labelString.setX(barMaxLength);
            valueString.setX(-4);
        } else {
            labelString.setX(0);
            valueString.setX(barMaxLength + 4);
        }

        labelString.setTextAlignment(alignment);
        valueString.setTextAlignment(alignment);
    }

    public void setValue(double value, double diffValue) {
        this.value = value;
        this.diffValue = diffValue;

        calculateBarLengths();
        updateValueLabel();
    }

    protected void calculateBarLengths() {
        double minValue = Math.min(value, diffValue);

        barLength = (int) Math.floor((minValue - min) / (max - min) * barMaxLength);
        diffLength = (int) Math.ceil( Math.abs(value - diffValue) / (max - min) * barMaxLength);

        diffColor = value < diffValue ? increaseColorBar : decreaseColorBar;
    }

    private void updateValueLabel() {
        if (value != diffValue) {
            if (alignment == GuiAlignment.right) {
                valueString.setString(String.format("%s(%+.02f) §r%.02f",
                    value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value, diffValue));
            } else {
                valueString.setString(String.format("%.02f %s(%+.02f)",
                    diffValue, value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value));
            }
        } else {
            valueString.setString(String.format("%.02f", diffValue));
        }
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        drawBar(refX, refY);
    }

    protected void drawBar(int refX, int refY) {
        drawRect(refX + x, refY + y + 6,refX + x + barMaxLength, refY + y + 6 + barHeight, 0x22ffffff);
        if (alignment == GuiAlignment.right) {
            drawRect(refX + x + barMaxLength - barLength, refY + y + 6,refX + x + barMaxLength, refY + y + 6 + barHeight, 0xffffffff);
            drawRect(refX + x + barMaxLength - barLength - diffLength, refY + y + 6,refX + x + barMaxLength - barLength, refY + y + 6 + barHeight,
                diffColor);
        } else {
            drawRect(refX + x, refY + y + 6,refX + x + barLength, refY + y + 6 + barHeight, 0xffffffff);
            drawRect(refX + x + barLength, refY + y + 6,refX + x + barLength + diffLength, refY + y + 6 + barHeight,
                diffColor);
        }
    }
}
