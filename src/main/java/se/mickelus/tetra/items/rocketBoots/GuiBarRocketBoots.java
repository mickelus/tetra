package se.mickelus.tetra.items.rocketBoots;

import se.mickelus.tetra.gui.GuiElement;

public class GuiBarRocketBoots extends GuiElement {

    private final int indicatorCount = 20;

    private int visibleIndicators = 0;
    private boolean shouldDraw = false;

    public GuiBarRocketBoots(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setFuel(float fuel) {
        shouldDraw = true;// fuel > 0 && fuel < 1;
        this.visibleIndicators = (int) (fuel * indicatorCount);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (shouldDraw) {
            for (int i = 0; i < visibleIndicators; i++) {
                drawRect(refX + 2 * i, refY, refX + 2 * i + 1, refY + 4, 0xffffffff);
            }
        }
    }
}
