package se.mickelus.tetra.gui.statbar;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiAlignment;

public class GuiStatBarCapability extends GuiStatBar {
    protected static final int efficiencyBarLength = 60;

    public GuiStatBarCapability(int x, int y, Capability capability) {
        super(x, y, efficiencyBarLength, I18n.format("capability." + capability.toString()), 0, 40);

        valueString.setX
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
