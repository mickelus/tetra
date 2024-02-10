package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.TweakData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class GuiTweakControls extends GuiElement {

    private final GuiString untweakableLabel;

    private final GuiElement tweakControls;
    private final GuiButton applyButton;

    private final Consumer<Map<String, Integer>> previewTweak;
    private final AnimationChain flash;

    private Map<String, Integer> tweaks;

    public GuiTweakControls(int x, int y, Consumer<Map<String, Integer>> previewTweak, Consumer<Map<String, Integer>> applyTweak) {
        super(x, y, 224, 67);

        addChild(new GuiTexture(-4, -4, 239, 70, 0, 48, GuiTextures.workbench));

        untweakableLabel = new GuiString(0, -3, ChatFormatting.DARK_GRAY + I18n.get("tetra.workbench.module_detail.not_tweakable"));
        untweakableLabel.setAttachment(GuiAttachment.middleCenter);
        addChild(untweakableLabel);

        tweakControls = new GuiElement(0, -4, width, height - 20);
        tweakControls.setAttachment(GuiAttachment.middleLeft);
        addChild(tweakControls);

        applyButton = new GuiButton(0, -10, I18n.get("tetra.workbench.slot_detail.tweak_apply"), () -> applyTweak.accept(tweaks));
        applyButton.setAttachment(GuiAttachment.bottomCenter);
        addChild(applyButton);

        this.previewTweak = previewTweak;

        tweaks = new HashMap<>();

        GuiTexture flashOverlay = new GuiTexture(-4, -4, 239, 70, 0, 48, GuiTextures.workbench);
        flashOverlay.setOpacity(0);
        flashOverlay.setColor(0);
        addChild(flashOverlay);
        flash = new AnimationChain(
                new KeyframeAnimation(40, flashOverlay).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(80, flashOverlay).applyTo(new Applier.Opacity(0)));
    }

    public void update(ItemModule module, ItemStack itemStack) {
        tweakControls.clearChildren();
        if (module != null && module.isTweakable(itemStack)) {
            TweakData[] data = module.getTweaks(itemStack);
            tweakControls.setHeight(data.length * 22);
            for (int i = 0; i < data.length; i++) {
                TweakData tweak = data[i];
                GuiTweakSlider slider = new GuiTweakSlider(0, i * 22, 200, tweak, step -> applyTweak(tweak.key, step));
                slider.setAttachment(GuiAttachment.topCenter);
                slider.setValue(module.getTweakStep(itemStack, tweak));
                tweakControls.addChild(slider);
            }

            applyButton.setVisible(true);
            untweakableLabel.setVisible(false);
        } else {
            applyButton.setVisible(false);
            untweakableLabel.setVisible(true);
        }
    }

    private void applyTweak(String key, int step) {
        tweaks.put(key, step);
        previewTweak.accept(tweaks);
    }

    public void flash() {
        this.flash.stop();
        this.flash.start();
    }
}
