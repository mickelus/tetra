package se.mickelus.tetra.gui.statbar;

import se.mickelus.tetra.gui.*;

public class GuiBar extends GuiElement {

    protected static final int increaseColorBar = 0x8855ff55;
    protected static final int decreaseColorBar = 0x88ff5555;
    protected int diffColor;

    protected double min;
    protected double max;

    protected double value;
    protected double diffValue;

    protected int barLength;
    protected int diffLength;

    protected GuiAlignment alignment = GuiAlignment.left;

    public GuiBar(int x, int y, int barLength, double min, double max) {
        super(x, y, barLength, 1);

        this.min = min;
        this.max = max;
    }

    public void setAlignment(GuiAlignment alignment) {
        this.alignment = alignment;
    }

    public void setValue(double value, double diffValue) {
        this.value = value;
        this.diffValue = diffValue;

        calculateBarLengths();
    }

    protected void calculateBarLengths() {
        double minValue = Math.min(value, diffValue);

        barLength = (int) Math.floor((minValue - min) / (max - min) * width);
        diffLength = (int) Math.ceil( Math.abs(value - diffValue) / (max - min) * width);

        diffColor = value < diffValue ? increaseColorBar : decreaseColorBar;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        drawRect(refX + x, refY + y + 6,refX + x + width, refY + y + 6 + height, 0x22ffffff);
        if (alignment == GuiAlignment.right) {
            drawRect(refX + x + width - barLength, refY + y + 6,refX + x + width, refY + y + 6 + height, 0xffffffff);
            drawRect(refX + x + width - barLength - diffLength, refY + y + 6,refX + x + width - barLength, refY + y + 6 + height,
                    diffColor);
        } else {
            drawRect(refX + x, refY + y + 6,refX + x + barLength, refY + y + 6 + height, 0xffffffff);
            drawRect(refX + x + barLength, refY + y + 6,refX + x + barLength + diffLength, refY + y + 6 + height,
                    diffColor);
        }
    }
}
