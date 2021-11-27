package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.ArrayList;

public class ScannerBarGui extends GuiElement {
    private AnimationChain[] upAnimations;
    private AnimationChain[] upHighlightAnimations;
    private AnimationChain[] midAnimations;
    private AnimationChain[] midHighlightAnimations;
    private AnimationChain[] downAnimations;
    private AnimationChain[] downHighlightAnimations;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    int horizontalSpread;

    private static final int unitWidth = 6;

    private boolean hasStatus;
    private GuiString statusLabel;
    private KeyframeAnimation statusInAnimation;
    private KeyframeAnimation statusOutAnimation;

    public ScannerBarGui(int x, int y, int horizontalSpread) {
        super(x, y, horizontalSpread * unitWidth, 9);

        this.horizontalSpread = horizontalSpread;

        statusLabel = new GuiStringOutline(-2, 0, "");
        statusLabel.setAttachment(GuiAttachment.middleCenter);
        statusLabel.setOpacity(0);
        addChild(statusLabel);
        statusInAnimation = new KeyframeAnimation(200, statusLabel)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(-2, 0));
        statusOutAnimation = new KeyframeAnimation(300, statusLabel)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(0, 2));


        showAnimation = new KeyframeAnimation(300, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y, y - 4));
        hideAnimation = new KeyframeAnimation(300, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 4, y))
                .withDelay(200)
                .onStop(complete -> this.isVisible = false);

        setup();
    }

    private void setup() {
        setWidth(horizontalSpread * unitWidth);

        upAnimations = new AnimationChain[horizontalSpread];
        midAnimations = new AnimationChain[horizontalSpread];
        downAnimations = new AnimationChain[horizontalSpread];
        upHighlightAnimations = new AnimationChain[horizontalSpread];
        midHighlightAnimations = new AnimationChain[horizontalSpread];
        downHighlightAnimations = new AnimationChain[horizontalSpread];

        // backdrop
        addChild(new GuiRect(-3, -2, getWidth() + 3, getHeight() + 2, 0).setOpacity(0.5f));

        // left caps
        addChild(new GuiTexture(-2, -1, 2, 2, 1, 1, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.topLeft));
        addChild(new GuiTexture(-2, -1, 2, 2, 1, 0, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.bottomLeft));

        // right caps
        addChild(new GuiTexture(-1, -1, 2, 2, 0, 1, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.topRight));
        addChild(new GuiTexture(-1, -1, 2, 2, 0, 0, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.bottomRight));

        // center "caps"
        addChild(new GuiTexture(-2, -1, 3, 2, 0, 1, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.topCenter));
        addChild(new GuiTexture(-2, -1, 3, 2, 0, 0, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.bottomCenter));

        for (int i = 0; i < horizontalSpread; i++) {
            GuiElement up = new GuiTexture(i * 6, 0, 3, 3, GuiTextures.hud).setOpacity(0.3f);
            addChild(up);
            upAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, up).applyTo(new Applier.Opacity(0.7f)),
                    new KeyframeAnimation(600, up).applyTo(new Applier.Opacity(0.3f)));


            GuiElement upHighlight = new GuiTexture(i * 6, 0, 3, 3, GuiTextures.hud)
                    .setColor(GuiColors.scanner)
                    .setOpacity(0);
            addChild(upHighlight);
            upHighlightAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, upHighlight).applyTo(new Applier.Opacity(0.9f)),
                    new KeyframeAnimation(1000, upHighlight).applyTo(new Applier.Opacity(0)));

            GuiElement down = new GuiTexture(i * 6, 4, 3, 3, GuiTextures.hud).setOpacity(0.3f);
            addChild(down);
            downAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, down).applyTo(new Applier.Opacity(0.7f)),
                    new KeyframeAnimation(600, down).applyTo(new Applier.Opacity(0.3f)));


            GuiElement downHighlight = new GuiTexture(i * 6, 4, 3, 3, GuiTextures.hud)
                    .setColor(GuiColors.scanner)
                    .setOpacity(0);
            addChild(downHighlight);
            downHighlightAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, downHighlight).applyTo(new Applier.Opacity(0.9f)),
                    new KeyframeAnimation(1000, downHighlight).applyTo(new Applier.Opacity(0)));
        }

        for (int i = 0; i < horizontalSpread - 1; i++) {
            GuiElement center = new GuiTexture(i * 6 + 3, 2, 3, 3, GuiTextures.hud).setOpacity(0.3f);
            addChild(center);
            midAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, center).applyTo(new Applier.Opacity(0.7f)),
                    new KeyframeAnimation(600, center).applyTo(new Applier.Opacity(0.3f)));

            GuiElement centerHighlight = new GuiTexture(i * 6 + 3, 2, 3, 3, GuiTextures.hud)
                    .setColor(GuiColors.scanner)
                    .setOpacity(0);
            addChild(centerHighlight);
            midHighlightAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, centerHighlight).applyTo(new Applier.Opacity(0.9f)),
                    new KeyframeAnimation(1000, centerHighlight).applyTo(new Applier.Opacity(0)));
        }

        addChild(statusLabel);
    }

    public static double getDegreesPerUnit() {
        return Minecraft.getInstance().gameSettings.fov * unitWidth / Minecraft.getInstance().getMainWindow().getScaledWidth();
    }

    public void setHorizontalSpread(int horizontalSpread) {
        if (horizontalSpread != 0 && this.horizontalSpread != horizontalSpread) {
            this.horizontalSpread = horizontalSpread;

            clearChildren();
            setup();
        }
    }

    public void setStatus(String status) {
        if ((status != null) != hasStatus) {
            hasStatus = status != null;

            if (hasStatus) {
                statusOutAnimation.stop();
                statusInAnimation.start();
            } else {
                statusInAnimation.stop();
                statusOutAnimation.start();
            }
        }

        statusLabel.setString(status);
    }

    protected void show() {
        isVisible = true;
        if (opacity < 1 && !showAnimation.isActive()) {
            isVisible = true;
            hideAnimation.stop();
            showAnimation.start();
        }
    }

    protected boolean hide() {
        if (opacity > 0 && !hideAnimation.isActive()) {
            showAnimation.stop();
            hideAnimation.start();
        }

        return false;
    }

    public void highlightUp(int index, boolean wasHit) {
        if (opacity == 1) {
            if (wasHit) {
                upHighlightAnimations[index].start();
            } else {
                upAnimations[index].start();
            }
        }
    }

    public void highlightMid(int index, boolean wasHit) {
        if (opacity == 1) {
            if (wasHit) {
                midHighlightAnimations[index].start();
            } else {
                midAnimations[index].start();
            }
        }
    }

    public void highlightDown(int index, boolean wasHit) {
        if (opacity == 1) {
            if (wasHit) {
                downHighlightAnimations[index].start();
            } else {
                downAnimations[index].start();
            }
        }
    }
}
