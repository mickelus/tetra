package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

public class GuiInventoryHighlight extends GuiElement {

    private AnimationChain animation;
    GuiElement dots;

    public GuiInventoryHighlight(int x, int y, int offset) {
        super(x, y, 16, 16);

        GuiTexture texture = new GuiTexture(0, 0, 16, 16, 80, 16, GuiTextures.workbench);
        addChild(texture);

        dots = new GuiElement(2, 2, 12, 12);
        dots.addChild(new GuiRect(0, 0, 1, 1, GuiColors.normal));
        dots.addChild(new GuiRect(0, 0, 1, 1, GuiColors.normal).setAttachment(GuiAttachment.topRight));
        dots.addChild(new GuiRect(0, 0, 1, 1, GuiColors.normal).setAttachment(GuiAttachment.bottomLeft));
        dots.addChild(new GuiRect(0, 0, 1, 1, GuiColors.normal).setAttachment(GuiAttachment.bottomRight));
        addChild(dots);

        animation = new AnimationChain(
                new KeyframeAnimation(200, texture)
                        .withDelay(offset * 50)
                        .applyTo(new Applier.Opacity(0, 1)),
                new KeyframeAnimation(300, texture)
                        .applyTo(new Applier.Opacity(0.4f)),
                new KeyframeAnimation(400, dots)
                        .applyTo(new Applier.Opacity(1))
        );
    }

    @Override
    protected void onShow() {
        dots.setOpacity(0);
        animation.start();
    }

    @Override
    protected boolean onHide() {
        animation.stop();
        return super.onHide();
    }
}
