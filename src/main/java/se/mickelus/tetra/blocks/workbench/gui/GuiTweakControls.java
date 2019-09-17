package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.gui.GuiAttachment;
import se.mickelus.tetra.gui.GuiButton;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.TweakData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GuiTweakControls extends GuiElement {

    private GuiString untweakableLabel;

    private GuiElement tweakControls;
    private GuiButton applyButton;

    private Consumer<Map<String, Integer>> previewTweak;

    private Map<String, Integer> tweaks;

    public GuiTweakControls(int x, int y, Consumer<Map<String, Integer>> previewTweak, Consumer<Map<String, Integer>> applyTweak) {
        super(x, y, 224, 67);

        untweakableLabel = new GuiString(0, -3, TextFormatting.DARK_GRAY + I18n.format("workbench.module_detail.not_tweakable"));
        untweakableLabel.setAttachment(GuiAttachment.middleCenter);
        addChild(untweakableLabel);

        tweakControls = new GuiElement(0, -4, width, height - 20);
        tweakControls.setAttachment(GuiAttachment.middleLeft);
        addChild(tweakControls);

        applyButton = new GuiButton(0, -10, I18n.format("workbench.slot_detail.tweak_apply"), () -> applyTweak.accept(tweaks));
        applyButton.setAttachment(GuiAttachment.bottomCenter);
        addChild(applyButton);

        this.previewTweak = previewTweak;

        tweaks = new HashMap<>();
    }

    public void update(ItemModule module, ItemStack itemStack) {
        if (module != null && module.isTweakable(itemStack)) {
            tweakControls.clearChildren();

            TweakData[] data = module.getTweaks(itemStack);
            tweakControls.setHeight(data.length * 22);
            for (int i = 0; i < data.length; i++) {
                TweakData tweak = data[0];
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
}
