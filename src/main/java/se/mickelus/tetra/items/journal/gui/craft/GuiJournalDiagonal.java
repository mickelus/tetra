package se.mickelus.tetra.items.journal.gui.craft;

import com.mojang.blaze3d.matrix.MatrixStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;

public class GuiJournalDiagonal extends GuiElement {

    protected AnimationChain animation;
    private static final float targetOpacity = 0.2f;

    protected boolean upRight = false;

    public GuiJournalDiagonal(int x, int y, int size, GuiAttachment attachment, int delay) {
        super(x, y, size, size);

        setAttachmentAnchor(GuiAttachment.middleCenter);
        setAttachmentPoint(attachment);

        if (attachment == GuiAttachment.bottomLeft || attachment == GuiAttachment.topRight) {
            upRight = true;
        }

        setOpacity(0);
        animation = new AnimationChain(
                new KeyframeAnimation(300, this).withDelay(delay).applyTo(new Applier.Opacity(targetOpacity + 0.1f)),
                new KeyframeAnimation(150, this).applyTo(new Applier.Opacity(targetOpacity)));
    }

    public void animateOpen() {
        animation.stop();
        setOpacity(0);
        animation.start();
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (upRight) {
            for (int i = 0; i < width; i++) {
                drawRect(matrixStack, refX + x + width - i - 1, refY + y + i, refX + x + width - i, refY + y + i + 1, GuiColors.normal, getOpacity() * opacity);
            }
        } else {
            for (int i = 0; i < width; i++) {
                drawRect(matrixStack, refX + x + i, refY + y + i, refX + x + i + 1, refY + y + i + 1, GuiColors.normal, getOpacity() * opacity);
            }
        }
    }
}
