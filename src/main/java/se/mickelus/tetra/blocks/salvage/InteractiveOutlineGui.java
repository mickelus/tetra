package se.mickelus.tetra.blocks.salvage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.TetraMod;

public class InteractiveOutlineGui extends GuiElement {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/block-interaction.png");

    private BlockInteraction blockInteraction;

    private GuiTexture topLeft;
    private GuiTexture topRight;
    private GuiTexture bottomLeft;
    private GuiTexture bottomRight;

    private InteractiveToolGui tool;

    public InteractiveOutlineGui(BlockInteraction blockInteraction, Player player) {
        super((int) blockInteraction.minX * 4, (int) blockInteraction.minY * 4,
                (int) (blockInteraction.maxX - blockInteraction.minX) * 4,
                (int) (blockInteraction.maxY - blockInteraction.minY) * 4);

        this.blockInteraction = blockInteraction;

        opacity = 0.5f;

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
                .onStop(complete -> {
                    if (tool != null) tool.updateFadeTime();
                })
                .start();

        if (blockInteraction.requiredTool != null) {
            tool = new InteractiveToolGui(0, 0, blockInteraction.requiredTool, blockInteraction.requiredLevel, player);
            addChild(tool);

             float centerY = y + height / 2f;
             float centerX = x + width / 2f;

            if (Math.abs(centerX - 16) > Math.abs(centerY - 16)) {
                if (centerX < 16) {
                    tool.setAttachmentPoint(GuiAttachment.middleLeft);
                    tool.setAttachmentAnchor(GuiAttachment.middleRight);
                    tool.setX(-1);
                } else {
                    tool.setAttachmentPoint(GuiAttachment.middleRight);
                    tool.setAttachmentAnchor(GuiAttachment.middleLeft);
                }
            } else {
                if (centerY < 16) {
                    tool.setAttachmentPoint(GuiAttachment.topCenter);
                    tool.setAttachmentAnchor(GuiAttachment.bottomCenter);
                    tool.setY(1);
                } else {
                    tool.setAttachmentPoint(GuiAttachment.bottomCenter);
                    tool.setAttachmentAnchor(GuiAttachment.topCenter);
                    tool.setY(-2);
                }
            }
        }
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        if (tool != null) {
            tool.show();
        }
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        if (tool != null) {
            tool.hide();
        }
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
