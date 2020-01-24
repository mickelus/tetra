package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiColors;
import se.mickelus.mgui.gui.impl.GuiKeybinding;
import se.mickelus.tetra.gui.GuiTextures;

public class GuiSlotTabButton extends GuiClickable {

    private boolean hasContent = false;
    private boolean isActive;

    private GuiRect indicator;
    private GuiTexture icon;
    private GuiString label;
    private GuiKeybinding keybinding;

    private AnimationChain indicatorFlash;
    private AnimationChain iconFlash;

    private KeyframeAnimation labelShow;
    private KeyframeAnimation labelHide;

    private KeyframeAnimation keybindShow;
    private KeyframeAnimation keybindHide;

    public GuiSlotTabButton(int x, int y, int i, String label, String keybinding, boolean initiallyActive, Runnable onClickHandler) {
        super(x, y, 0, 15, onClickHandler);

        setAttachmentPoint(GuiAttachment.topRight);

        indicator = new GuiRect(0, 0, 1, 15, GuiColors.normal);
        indicator.setAttachment(GuiAttachment.topRight);
        addChild(indicator);
        indicatorFlash = new AnimationChain(
                new KeyframeAnimation(40, indicator).applyTo(new Applier.TranslateX(-3)),
                new KeyframeAnimation(60, indicator).applyTo(new Applier.TranslateX(0)));

        icon = new GuiTexture(-3, 3, 10, 9, 128 + i * 16, 32, GuiTextures.workbench);
        icon.setAttachment(GuiAttachment.topRight);
        addChild(icon);
        iconFlash = new AnimationChain(
                new KeyframeAnimation(40, icon).applyTo(new Applier.TranslateX(-4)),
                new KeyframeAnimation(60, icon).applyTo(new Applier.TranslateX(-3)));

        this.label = new GuiString(-16, 4, label);
        this.label.setAttachment(GuiAttachment.topRight);
        this.label.setOpacity(0);
        addChild(this.label);
        labelShow = new KeyframeAnimation(100, this.label).applyTo(new Applier.Opacity(1), new Applier.TranslateX(-16));
        labelHide = new KeyframeAnimation(150, this.label).applyTo(new Applier.Opacity(0), new Applier.TranslateX(-13));

        this.keybinding = new GuiKeybinding(0, 2, keybinding);
        this.keybinding.setOpacity(0);
        keybindShow = new KeyframeAnimation(100, this.keybinding).applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));
        keybindHide = new KeyframeAnimation(150, this.keybinding).applyTo(new Applier.Opacity(0), new Applier.TranslateX(3));
        addChild(this.keybinding);


        width = this.label.getWidth() + 20;


        isActive = initiallyActive;
        updateStyling();
    }

    private void updateStyling() {
        if (isActive) {
            indicator.setOpacity(1);
            icon.setColor(hasFocus() ? GuiColors.hover : GuiColors.normal);
        } else if (hasContent) {
            indicator.setOpacity(0.5f);
            icon.setColor(hasFocus() ? GuiColors.hoverMuted : GuiColors.muted);
        } else {
            indicator.setOpacity(0.25f);
            icon.setColor(hasFocus() ? GuiColors.hoverMuted : GuiColors.muted);
        }

        indicator.setColor(hasFocus() ? GuiColors.hover : GuiColors.normal);
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
            iconFlash.stop();
            iconFlash.start();
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
