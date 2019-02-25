package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.AnimationChain;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiCompartmentButton extends GuiClickable {

    private boolean hasContent = false;
    private boolean isActive = false;

    private GuiRect indicator;
    private GuiString label;
    private GuiKeybinding keybinding;

    private AnimationChain indicatorFlash;

    private KeyframeAnimation labelShow;
    private KeyframeAnimation labelHide;

    private KeyframeAnimation keybindShow;
    private KeyframeAnimation keybindHide;

    public GuiCompartmentButton(int x, int y, int index, String keybinding, Runnable onClickHandler) {
        super(x, y, 0, 15, onClickHandler);

        setAttachmentPoint(GuiAttachment.topRight);

        indicator = new GuiRect(0, 0, 1, 15, GuiColors.normal);
        indicator.setAttachment(GuiAttachment.topRight);
        addChild(indicator);
        indicatorFlash = new AnimationChain(
                new KeyframeAnimation(40, indicator).applyTo(new Applier.TranslateX(-3)),
                new KeyframeAnimation(60, indicator).applyTo(new Applier.TranslateX(0)));

        label = new GuiString(-5, 4, I18n.format("forged_container.compartment", index + 1));
        label.setAttachment(GuiAttachment.topRight);
        label.setOpacity(0);
        addChild(label);
        labelShow = new KeyframeAnimation(100, label).applyTo(new Applier.Opacity(1), new Applier.TranslateX(-5));
        labelHide = new KeyframeAnimation(150, label).applyTo(new Applier.Opacity(0), new Applier.TranslateX(-2));

        this.keybinding = new GuiKeybinding(0, 2, keybinding);
        this.keybinding.setOpacity(0);
        addChild(this.keybinding);
        keybindShow = new KeyframeAnimation(100, this.keybinding).applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));
        keybindHide = new KeyframeAnimation(150, this.keybinding).applyTo(new Applier.Opacity(0), new Applier.TranslateX(3));

        width = label.getWidth() + 10;

        if (index == 0) {
            isActive = true;
            updateStyling();
        }
    }

    private void updateStyling() {
        if (isActive) {
            indicator.setOpacity(1);
        } else if (hasContent) {
            indicator.setOpacity(0.5f);
        } else {
            indicator.setOpacity(0.25f);
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
