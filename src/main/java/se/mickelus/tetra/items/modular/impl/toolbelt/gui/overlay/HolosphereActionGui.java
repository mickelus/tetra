package se.mickelus.tetra.items.modular.impl.toolbelt.gui.overlay;

import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiVerticalLayoutGroup;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.Arrays;

public class HolosphereActionGui extends GuiElement {
    private final KeyframeAnimation showAnimation;

    private final KeyframeAnimation focusLabel;
    private final KeyframeAnimation blurLabel;

    GuiTexture backdrop;
    GuiTexture icon;

    Runnable performRunnable;

    public HolosphereActionGui(int x, int y, int u, int v, String label, Runnable perform) {
        super(x, y, 23, 23);

        this.performRunnable = perform;

        backdrop = new GuiTexture(0, 0, 23, 23, 55, 28, GuiTextures.toolbelt);
        addChild(backdrop);

        icon = new GuiTexture(0, 0, 23, 23, u, v, GuiTextures.toolbelt);
        addChild(icon);

        GuiElement labelContainer = new GuiVerticalLayoutGroup(2, 1, 0, -1);
        Arrays.stream(label.split("\n")).map(l -> new GuiStringOutline(0, 0, l).setAttachment(GuiAttachment.topCenter)).forEach(labelContainer::addChild);
        labelContainer.setAttachment(GuiAttachment.middleCenter);
        labelContainer.setOpacity(0);
        addChild(labelContainer);


        isVisible = false;

        showAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.TranslateY(y + 2, y), new Applier.Opacity(0, 1))
                .withDelay((int) (Math.random() * 300));


        focusLabel = new KeyframeAnimation(80, labelContainer)
                .applyTo(new Applier.TranslateY(-2, 0), new Applier.Opacity(1));

        blurLabel = new KeyframeAnimation(120, labelContainer)
                .applyTo(new Applier.TranslateY(0, 2), new Applier.Opacity(0));
    }

    public void perform() {
        this.performRunnable.run();
    }

    @Override
    protected void onShow() {
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        if (showAnimation.isActive()) {
            showAnimation.stop();
        }
        return true;
    }

    @Override
    protected void onFocus() {
        backdrop.setColor(GuiColors.hover);
        icon.setColor(GuiColors.muted);
        blurLabel.stop();
        focusLabel.start();
    }

    @Override
    protected void onBlur() {
        backdrop.setColor(GuiColors.normal);
        icon.setColor(GuiColors.normal);
        focusLabel.stop();
        blurLabel.start();
    }

    @Override
    public void updateFocusState(int refX, int refY, int mouseX, int mouseY) {
        float ox = mouseX - refX - x - width / 2f;
        float oy = mouseY - refY - y - height / 2f;
        boolean gainFocus = Math.abs(ox + oy) <= width / 2f + 1 && Math.abs(ox - oy) <= width / 2f + 1;

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }
}
