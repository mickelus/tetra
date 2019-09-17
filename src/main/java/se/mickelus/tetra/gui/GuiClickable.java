package se.mickelus.tetra.gui;

public class GuiClickable extends GuiElement {

    protected final Runnable onClickHandler;

    public GuiClickable(int x, int y, int width, int height, Runnable onClickHandler) {
        super(x, y, width, height);

        this.onClickHandler = onClickHandler;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
    }

    @Override
    public boolean onClick(int x, int y) {
        if (hasFocus()) {
            onClickHandler.run();
            return true;
        }

        return false;
    }
}
