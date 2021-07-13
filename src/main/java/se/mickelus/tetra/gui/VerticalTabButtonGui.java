package se.mickelus.tetra.gui;

import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiColors;

public class VerticalTabButtonGui extends GuiClickable {

    protected boolean hasContent = false;
    protected boolean isActive;

    protected GuiRect indicator;
    protected GuiString label;
    protected GuiKeybinding keybinding;

    protected AnimationChain indicatorFlash;

    protected KeyframeAnimation labelShow;
    protected KeyframeAnimation labelHide;

    protected KeyframeAnimation keybindShow;
    protected KeyframeAnimation keybindHide;

    public VerticalTabButtonGui(int x, int y, String label, String keybinding, Runnable onClickHandler, boolean initiallyActive) {
        super(x, y, 0, 15, onClickHandler);

        setAttachmentPoint(GuiAttachment.topRight);

        indicator = new GuiRect(0, 0, 1, 15, se.mickelus.mgui.gui.impl.GuiColors.normal);
        indicator.setAttachment(GuiAttachment.topRight);
        addChild(indicator);
        indicatorFlash = new AnimationChain(
                new KeyframeAnimation(40, indicator).applyTo(new Applier.TranslateX(-3)),
                new KeyframeAnimation(60, indicator).applyTo(new Applier.TranslateX(0)));

        this.label = new GuiString(-5, 4, label);
        this.label.setAttachment(GuiAttachment.topRight);
        this.label.setOpacity(0);
        addChild(this.label);
        labelShow = new KeyframeAnimation(100, this.label).applyTo(new Applier.Opacity(1), new Applier.TranslateX(-5));
        labelHide = new KeyframeAnimation(150, this.label).applyTo(new Applier.Opacity(0), new Applier.TranslateX(-2));

        this.keybinding = new GuiKeybinding(0, 2, keybinding);
        this.keybinding.setAttachmentPoint(GuiAttachment.topRight);
        this.keybinding.setOpacity(0);
        keybindShow = new KeyframeAnimation(100, this.keybinding).applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));
        keybindHide = new KeyframeAnimation(150, this.keybinding).applyTo(new Applier.Opacity(0), new Applier.TranslateX(3));
        if (keybinding != null) {
            addChild(this.keybinding);
        }

        width = this.label.getWidth() + 10;


        isActive = initiallyActive;
        updateStyling();
    }

    protected void updateStyling() {
        if (isActive) {
            indicator.setOpacity(1);
        } else if (hasContent) {
            indicator.setOpacity(0.5f);
        } else {
            indicator.setOpacity(0.25f);
        }

        indicator.setColor(hasFocus() ? se.mickelus.mgui.gui.impl.GuiColors.hover : GuiColors.normal);
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
        updateStyling();
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        updateStyling();

        if (isActive) {
            indicatorFlash.stop();
            indicatorFlash.start();
        }
    }

    @Override
    protected void onFocus() {
        updateStyling();
        labelHide.stop();
        keybindHide.stop();
        labelShow.stop();
        keybindShow.stop();

        labelShow.start();
        keybindShow.start();
    }

    @Override
    protected void onBlur() {
        updateStyling();
        labelShow.stop();
        keybindShow.stop();
        labelHide.stop();
        keybindHide.stop();

        labelHide.start();
        keybindHide.start();
    }
}
