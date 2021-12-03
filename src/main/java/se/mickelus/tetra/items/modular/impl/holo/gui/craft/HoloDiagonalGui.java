package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.mojang.blaze3d.vertex.PoseStack;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class HoloDiagonalGui extends GuiElement {

    protected AnimationChain openAnimation;
    protected AnimationChain reopenAnimation;
    private static final float targetOpacity = 0.2f;

    protected boolean upRight = false;

    public HoloDiagonalGui(int x, int y, int size, GuiAttachment attachment, int delay) {
        super(x, y, size, size);

        setAttachmentAnchor(GuiAttachment.middleCenter);
        setAttachmentPoint(attachment);

        if (attachment == GuiAttachment.bottomLeft || attachment == GuiAttachment.topRight) {
            upRight = true;
        }

        setOpacity(0);
        openAnimation = new AnimationChain(
                new KeyframeAnimation(300, this).withDelay(delay).applyTo(new Applier.Opacity(targetOpacity + 0.1f)),
                new KeyframeAnimation(150, this).applyTo(new Applier.Opacity(targetOpacity)));

        reopenAnimation = new AnimationChain(
                new KeyframeAnimation(300, this).withDelay(delay / 10).applyTo(new Applier.Opacity(targetOpacity + 0.6f)),
                new KeyframeAnimation(150, this).applyTo(new Applier.Opacity(targetOpacity)));
    }

    public void animateOpen() {
        openAnimation.stop();
        reopenAnimation.stop();
        setOpacity(0);
        openAnimation.start();
    }

    public void animateReopen() {
        openAnimation.stop();
        reopenAnimation.stop();
        setOpacity(0);
        reopenAnimation.start();
    }

    public void stopAnimations() {
        openAnimation.stop();
        reopenAnimation.stop();
        setOpacity(0);
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
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
