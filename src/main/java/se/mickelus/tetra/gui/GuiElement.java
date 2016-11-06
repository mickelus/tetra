package se.mickelus.tetra.gui;

public class GuiElement extends GuiNode {

    protected int x;
    protected int y;

    protected int width;
    protected int height;

    protected boolean hasFocus = false;

    public GuiElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        calculateFocusState(refX, refY, mouseX, mouseY);
    }

    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        boolean gainFocus = mouseX >= x + refX && mouseX <= x + refX + width && mouseY >= y + refY && mouseY <= y + refY + height;

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
        }
    }

    public boolean hasFocus() {
        return hasFocus;
    }
}
