package se.mickelus.tetra.gui;

public class GuiClickable extends GuiElement {

    protected final Runnable onClickHandler;
    /* buttons position on the actual screen, updated on draw to be used when calculating if click events hit this button */
    private int screenX;
    private int screenY;

    public GuiClickable(int x, int y, int width, int height, Runnable onClickHandler) {
        super(x, y, width, height);

        this.onClickHandler = onClickHandler;
    }

    protected void updateScreenPosition(int screenX, int screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        updateScreenPosition(refX + x, refY + y);
    }

    @Override
    public boolean onClick(int x, int y) {
        if (x >= screenX && x <= screenX + width && y >= screenY && y <= screenY + height) {
            onClickHandler.run();
            return true;
        }

        return false;
    }
}
