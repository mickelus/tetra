package se.mickelus.tetra.blocks.salvage;

import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiInteractiveOutline extends GuiElement {

    private static final String texture = "textures/gui/block_interaction.png";

    private BlockInteraction blockInteraction;

    private GuiTexture topLeft;
    private GuiTexture topRight;
    private GuiTexture bottomLeft;
    private GuiTexture bottomRight;

    public GuiInteractiveOutline(BlockInteraction blockInteraction) {
        super((int) blockInteraction.minX, (int) blockInteraction.minY,
                (int) (blockInteraction.maxX - blockInteraction.minX),
                (int) (blockInteraction.maxY - blockInteraction.minY));

        this.blockInteraction = blockInteraction;

        opacity = 0.3f;

        topLeft = new GuiTexture(-2, -2, 4, 4, 0, 0, texture);
        addChild(topLeft);
        new KeyframeAnimation(100, topLeft)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, -2),
                        new Applier.TranslateY(0, -2))
                .withDelay(500)
                .start();

        topRight = new GuiTexture(2, -2, 4, 4, 3, 0, texture);
        topRight.setAttachment(GuiAttachment.topRight);
        addChild(topRight);
        new KeyframeAnimation(100, topRight)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, 2),
                        new Applier.TranslateY(0, -2))
                .withDelay(650)
                .start();

        bottomLeft = new GuiTexture(-2, 2, 4, 4, 3, 0, texture);
        bottomLeft.setAttachment(GuiAttachment.bottomLeft);
        addChild(bottomLeft);
        new KeyframeAnimation(100, bottomLeft)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, -2),
                        new Applier.TranslateY(0, 2))
                .withDelay(500)
                .start();

        bottomRight = new GuiTexture(2, 2, 4, 4, 0, 0, texture);
        bottomRight.setAttachment(GuiAttachment.bottomRight);
        addChild(bottomRight);
        new KeyframeAnimation(100, bottomRight)
                .applyTo(new Applier.Opacity(0, 1),
                        new Applier.TranslateX(0, 2),
                        new Applier.TranslateY(0, 2))
                .withDelay(650)
                .start();
    }

    public BlockInteraction getBlockInteraction() {
        return blockInteraction;
    }

    public void transitionOut(Runnable onStop) {
        new KeyframeAnimation(200, topLeft)
                .applyTo(new Applier.Opacity(1, 0),
                        new Applier.TranslateX(-5),
                        new Applier.TranslateY(-5))
                .start();

        new KeyframeAnimation(200, topRight)
                .applyTo(new Applier.Opacity(1, 0),
                        new Applier.TranslateX(5),
                        new Applier.TranslateY(-5))
                .start();

        new KeyframeAnimation(200, bottomLeft)
                .applyTo(new Applier.Opacity(1, 0),
                        new Applier.TranslateX(-5),
                        new Applier.TranslateY(5))
                .start();

        new KeyframeAnimation(200, bottomRight)
                .applyTo(new Applier.Opacity(1, 0),
                        new Applier.TranslateX(5),
                        new Applier.TranslateY(5))
                .onStop(finished -> onStop.run())
                .start();
    }
}
