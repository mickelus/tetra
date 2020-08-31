package se.mickelus.tetra.items.modular.impl.holo.gui;

import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;

public class HoloRootBaseGui extends GuiElement {

    KeyframeAnimation showAnimation;
    KeyframeAnimation hideAnimation;

    public HoloRootBaseGui(int x, int y) {
        super(x, y, 320, 205);

        showAnimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y - 5, y), new Applier.Opacity(0, 1))
                .withDelay(50);
        hideAnimation = new KeyframeAnimation(50, this)
                .applyTo(new Applier.TranslateY(y - 5, y), new Applier.Opacity(0, 1));
    }

    public void animateOpen() {
        new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .withDelay(800)
                .start();
    }

    public void charTyped(char typedChar) {

    }

    @Override
    protected void onShow() {
        hideAnimation.stop();
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        showAnimation.stop();
        hideAnimation.start();
        return super.onHide();
    }

    public void onReload() {

    }
}
