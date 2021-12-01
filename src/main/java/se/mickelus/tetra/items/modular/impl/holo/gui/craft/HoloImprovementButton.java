package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.ArrayList;
import java.util.List;

public class HoloImprovementButton extends GuiClickable {
    GuiString label;
    List<KeyframeAnimation> hoverAnimations;
    List<KeyframeAnimation> blurAnimations;

    boolean hasImprovements = false;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public HoloImprovementButton(int x, int y, Runnable onClick) {
        super(x, y, 0,  19, onClick);

        label = new GuiStringOutline(0, 0, I18n.get("tetra.holo.craft.improvement_button", "00"));
        label.setAttachment(GuiAttachment.middleCenter);
        width = label.getWidth() + 16;

        hoverAnimations = new ArrayList<>();
        blurAnimations = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int texX = i * -5 + 8;
            GuiTexture texture = new GuiTexture(texX, 0, 15, 15, 222, 16, GuiTextures.workbench);
            texture.setAttachment(GuiAttachment.middleLeft);
            addChild(texture);

            hoverAnimations.add(new KeyframeAnimation(100, texture)
                    .applyTo(new Applier.TranslateX(texX - i * 3 - 2), new Applier.TranslateY(0))
                    .withDelay((3 - i) * 40));
            blurAnimations.add(new KeyframeAnimation(100, texture)
                    .applyTo(new Applier.TranslateX(texX), new Applier.TranslateY(0))
                    .withDelay(i * 40));
        }

        for (int i = 0; i < 3; i++) {
            int texX = i * 5 - 8;
            GuiTexture texture = new GuiTexture(texX, 0, 15, 15, 237, 16, GuiTextures.workbench);
            texture.setAttachment(GuiAttachment.middleRight);
            addChild(texture);

            hoverAnimations.add(new KeyframeAnimation(100, texture)
                    .applyTo(new Applier.TranslateX(texX + i * 3 + 2), new Applier.TranslateY(0))
                    .withDelay((3 - i) * 40));
            blurAnimations.add(new KeyframeAnimation(100, texture)
                    .applyTo(new Applier.TranslateX(texX), new Applier.TranslateY(0))
                    .withDelay(i * 40));
        }

        addChild(new GuiTexture(-26, 1, 11, 11, 193, 31, GuiTextures.workbench).setAttachment(GuiAttachment.middleLeft));
        addChild(new GuiTexture(26, 1, 11, 11, 193, 31, GuiTextures.workbench).setAttachment(GuiAttachment.middleRight));

        addChild(label);


        showAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(1))
                .withDelay(100);

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0))
                .onStop(complete -> {
                    if (complete) {
                        this.isVisible = false;
                    }
                });
    }

    public void updateCount(int count) {
        label.setString(I18n.get("tetra.holo.craft.improvement_button", count));

        hasImprovements = count > 0;
        label.setColor(hasImprovements ? GuiColors.normal : GuiColors.muted);
        blurAnimations.forEach(KeyframeAnimation::start);
    }

    public void show() {
        hideAnimation.stop();
        setVisible(true);
        setOpacity(0);
        showAnimation.start();
    }

    public void hide() {
        showAnimation.stop();
        hideAnimation.start();
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (hasImprovements) {
            return super.onMouseClick(x, y, button);
        }

        return false;
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        if (hasImprovements) {
            label.setColor(GuiColors.hover);
            blurAnimations.forEach(KeyframeAnimation::stop);
            hoverAnimations.forEach(KeyframeAnimation::start);
        }
    }

    @Override
    protected void onBlur() {
        super.onBlur();


        if (hasImprovements) {
            label.setColor(GuiColors.normal);
            hoverAnimations.forEach(KeyframeAnimation::stop);
            blurAnimations.forEach(KeyframeAnimation::start);
        }
    }
}
