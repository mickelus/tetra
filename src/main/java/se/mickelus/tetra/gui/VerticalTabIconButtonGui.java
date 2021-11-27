package se.mickelus.tetra.gui;

import net.minecraft.util.ResourceLocation;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiColors;
import se.mickelus.tetra.gui.VerticalTabButtonGui;
import se.mickelus.tetra.gui.GuiTextures;

public class VerticalTabIconButtonGui extends VerticalTabButtonGui {
    private GuiTexture icon;
    private AnimationChain iconFlash;

    public VerticalTabIconButtonGui(int x, int y, ResourceLocation texture, int textureX, int textureY, String label, String keybinding,
            Runnable onClickHandler, boolean initiallyActive) {
        super(x, y, label, keybinding, onClickHandler, initiallyActive);

        icon = new GuiTexture(-3, 3, 10, 9, textureX, textureY, texture);
        icon.setAttachment(GuiAttachment.topRight);
        addChild(icon);
        iconFlash = new AnimationChain(
                new KeyframeAnimation(40, icon).applyTo(new Applier.TranslateX(-4)),
                new KeyframeAnimation(60, icon).applyTo(new Applier.TranslateX(-3)));

        this.label.setX(-16);
        labelShow = new KeyframeAnimation(100, this.label).applyTo(new Applier.Opacity(1), new Applier.TranslateX(-16));
        labelHide = new KeyframeAnimation(150, this.label).applyTo(new Applier.Opacity(0), new Applier.TranslateX(-13));

        width = this.label.getWidth() + 20;

        updateStyling();
    }

    @Override
    protected void updateStyling() {
        super.updateStyling();

        // todo: nullcheck required as this is called in super constructor, can fix?
        if (icon !=  null) {
            if (isActive) {
                icon.setColor(hasFocus() ? GuiColors.hover : GuiColors.normal);
            } else if (hasContent) {
                icon.setColor(hasFocus() ? GuiColors.hoverMuted : GuiColors.muted);
            } else {
                icon.setColor(hasFocus() ? GuiColors.hoverMuted : GuiColors.muted);
            }
        }
    }

    @Override
    public void setActive(boolean isActive) {
        super.setActive(isActive);
        if (isActive) {
            iconFlash.stop();
            iconFlash.start();
        }
    }
}
