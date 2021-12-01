package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.VisibilityFilter;

public class GuiBarBooster extends GuiElement {

    private final int indicatorCount = 20;
    private final VisibilityFilter filter;

    private int visibleIndicators = 0;

    public GuiBarBooster(int x, int y, int width, int height) {
        super(x, y, width, height);
        opacity = 0;
        filter = new VisibilityFilter(0, indicatorCount);
    }

    public void setFuel(float fuel) {
        this.visibleIndicators = Math.round(fuel * indicatorCount);
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        opacity = filter.apply(visibleIndicators) * opacity;
        if (opacity > 0) {
            matrixStack.translate(opacity * -10, 0, 0);


            for (int i = 0; i < visibleIndicators; i++) {
                drawRect(matrixStack,
                        refX + x + 2 * i,
                        refY + y,
                        refX + x + 2 * i + 1,
                        refY + y + 4,
                        0xffffff, opacity * 0.9f);
            }

            for (int i = visibleIndicators; i < indicatorCount; i++) {
                drawRect(matrixStack,
                        refX + x + 2 * i,
                        refY + y,
                        refX + x + 2 * i + 1,
                        refY + y + 4,
                        0x000000, opacity * 0.3f);
            }

            drawRect(matrixStack,
                    refX + x - 2,
                    refY + y + 3,
                    refX + x - 1,
                    refY + y + 5,
                    0xffffff, opacity * 0.3f);

            drawRect(matrixStack,
                    refX + x - 2,
                    refY + y + 5,
                    refX + x + 10,
                    refY + y + 6,
                    0xffffff, opacity * 0.3f);
            matrixStack.translate(opacity * 10, 0, 0);
        }
    }
}
