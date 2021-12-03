package se.mickelus.tetra.gui.stats.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.tetra.gui.GuiColors;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class GuiBarSplit extends GuiBar {

    private GuiBar negativeBar;
    private GuiBar positiveBar;

    public GuiBarSplit(int x, int y, int barLength, double range, boolean inverted) {
        super(x, y, barLength, -range, range, inverted);

        negativeBar = new GuiBar(0, 0, (barLength - 5) / 2, 0, range, !inverted);
        negativeBar.setAlignment(GuiAlignment.right);
        addChild(negativeBar);

        positiveBar = new GuiBar(0, 0, (barLength - 5) / 2, 0, range, inverted);
        positiveBar.setAttachment(GuiAttachment.topRight);
        addChild(positiveBar);

        GuiElement separator = new GuiRect(0, 5, 1, 3, GuiColors.muted);
        separator.setAttachment(GuiAttachment.topCenter);
        addChild(separator);
    }

    protected void calculateBarLengths() {
        negativeBar.setValue(value > 0 ? 0 : -value, diffValue > 0 ? 0 : -diffValue);
        positiveBar.setValue(value < 0 ? 0 : value, diffValue < 0 ? 0 : diffValue);
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        drawChildren(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
    }
}
