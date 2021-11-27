package se.mickelus.tetra.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiClickable;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;

import java.util.function.Consumer;

public class GuiSliderSegmented extends GuiClickable {

    private boolean isDragging = false;

    private int valueSteps;
    private int value = 0;

    private Consumer<Integer> onChange;

    private GuiElement currentIndicator;
    private GuiElement hoverIndicator;

    public GuiSliderSegmented(int x, int y, int width, int valueSteps, Consumer<Integer> onChange) {
        super(x, y, width, 12, () -> {});

        addChild(new GuiRect(5, 7, width - 9, 1, GuiColors.muted).setOpacity(0.7f));

        addChild(new GuiRect(0, 7, 4, 1, GuiColors.muted));
        addChild(new GuiRect(1, 7, 4, 1, GuiColors.muted).setAttachment(GuiAttachment.topRight));
        addChild(new GuiRect(0, 4, 1, 3, GuiColors.muted));
        addChild(new GuiRect(1, 4, 1, 3, GuiColors.muted).setAttachment(GuiAttachment.topRight));

        for (int i = 0; i < valueSteps; i++) {
            if (((valueSteps - 1) / 2 - i) % 3 == 0) {
                addChild(new GuiRect(i * width / (valueSteps - 1) - 1, 7, 3, 1, 0));
                addChild(new GuiRect(i * width / (valueSteps - 1), 4, 1, 4, GuiColors.muted));
            } else {
                addChild(new GuiRect(i * width / (valueSteps - 1), 5, 1, 2, GuiColors.muted).setOpacity(0.7f));
            }
        }

        hoverIndicator = new GuiElement(0, 4, 1, 4);
        hoverIndicator.addChild(new GuiRect(-1, 0, 3, 5, 0));
        hoverIndicator.addChild(new GuiRect(0, -1, 1, 3, GuiColors.muted));
        hoverIndicator.addChild(new GuiRect(0, 3, 1, 1, GuiColors.muted));
        hoverIndicator.addChild(new GuiRect(0, 5, 1, 1, GuiColors.muted));
        hoverIndicator.setVisible(false);
        addChild(hoverIndicator);

        currentIndicator = new GuiElement(0, 4, 1, 4);
        currentIndicator.addChild(new GuiRect(-1, 0, 3, 5, 0));
        currentIndicator.addChild(new GuiRect(0, -1, 1, 3, GuiColors.normal));
        currentIndicator.addChild(new GuiRect(0, 3, 1, 1, GuiColors.selected));
        currentIndicator.addChild(new GuiRect(0, 5, 1, 1, GuiColors.normal));
        addChild(currentIndicator);

        this.valueSteps = valueSteps;

        this.onChange = onChange;
    }

    public void setValue(int value) {
        this.value = value;
        currentIndicator.setX(value * width / (valueSteps - 1));
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (super.onMouseClick(x, y, button)) {
            isDragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void onMouseRelease(int x, int y, int button) {
        isDragging = false;
    }

    protected int calculateSegment(int refX, int mouseX) {
        return Math.round((valueSteps - 1) * Math.min(Math.max((mouseX - refX - x) / (1f * width), 0), 1));
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        boolean gainFocus = mouseX >= getX() + refX - 5
                && mouseX < getX() + refX + getWidth() + 10
                && mouseY >= getY() + refY
                && mouseY < getY() + refY + getHeight();

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }

    @Override
    protected void onFocus() {
        hoverIndicator.setVisible(true);
    }

    @Override
    protected void onBlur() {
        hoverIndicator.setVisible(false);
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (isDragging) {
            int newSegment = calculateSegment(refX, mouseX);
            if (newSegment != value) {
                value = newSegment;
                onChange.accept(value);
                currentIndicator.setX(value * width / (valueSteps - 1));
                hoverIndicator.setX(value * width / (valueSteps - 1));
            }
        } else if (hoverIndicator.isVisible()) {
            hoverIndicator.setX(calculateSegment(refX, mouseX) * width / (valueSteps - 1));
        }

        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
    }
}
