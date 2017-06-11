package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.client.renderer.GlStateManager;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.filter.VisibilityFilter;

public class GuiBarRocketBoots extends GuiElement {

    private final int indicatorCount = 20;
    private final VisibilityFilter filter;

    private int visibleIndicators = 0;

    public GuiBarRocketBoots(int x, int y, int width, int height) {
        super(x, y, width, height);
        filter = new VisibilityFilter(-1, indicatorCount);
    }

    public void setFuel(float fuel) {
        this.visibleIndicators = Math.round(fuel * indicatorCount);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        float opacity = filter.apply(visibleIndicators);
        if (opacity > 0) {
            GlStateManager.translate(opacity * -10, 0, 0);
            for (int i = 0; i < visibleIndicators; i++) {
                drawRect(
                        refX + x + 2 * i,
                        refY + y,
                        refX + x + 2 * i + 1,
                        refY + y + 4,
                        0xffffff, opacity * 0.7f);
            }
            drawRect(
                    refX + x - 2,
                    refY + y + 3,
                    refX + x - 1,
                    refY + y + 5,
                    0xffffff, opacity * 0.3f);

            drawRect(
                    refX + x - 2,
                    refY + y + 5,
                    refX + x + 10,
                    refY + y + 6,
                    0xffffff, opacity * 0.3f);
            GlStateManager.translate(opacity * 10, 0, 0);
        }
    }
}
