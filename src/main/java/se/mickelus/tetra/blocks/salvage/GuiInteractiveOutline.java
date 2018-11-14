package se.mickelus.tetra.blocks.salvage;

import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiInteractiveOutline extends GuiElement {

    private static final String texture = "textures/gui/workbench.png";

    public GuiInteractiveOutline(BlockInteraction blockInteraction) {
        super((int) blockInteraction.minX, (int) blockInteraction.minY,
                (int) (blockInteraction.maxX - blockInteraction.minX),
                (int) (blockInteraction.maxY - blockInteraction.minY));

        opacity = 0.3f;

        GuiTexture topLeft = new GuiTexture(-2, -2, 4, 4, 68, 23, texture);
        addChild(topLeft);
        new KeyframeAnimation(100, topLeft)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, -2),
                        new Applier.TranslateY(0, -2))
                .withDelay(500)
                .start();

        GuiTexture topRight = new GuiTexture(2, -2, 4, 4, 71, 23, texture);
        topRight.setAttachment(GuiAttachment.topRight);
        addChild(topRight);
        new KeyframeAnimation(100, topRight)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, 2),
                        new Applier.TranslateY(0, -2))
                .withDelay(650)
                .start();

        GuiTexture bottomLeft = new GuiTexture(-2, 2, 4, 4, 71, 23, texture);
        bottomLeft.setAttachment(GuiAttachment.bottomLeft);
        addChild(bottomLeft);
        new KeyframeAnimation(100, bottomLeft)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, -2),
                        new Applier.TranslateY(0, 2))
                .withDelay(500)
                .start();

        GuiTexture bottomRight = new GuiTexture(2, 2, 4, 4, 68, 23, texture);
        bottomRight.setAttachment(GuiAttachment.bottomRight);
        addChild(bottomRight);
        new KeyframeAnimation(100, bottomRight)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, 2),
                        new Applier.TranslateY(0, 2))
                .withDelay(650)
                .start();
    }
}
