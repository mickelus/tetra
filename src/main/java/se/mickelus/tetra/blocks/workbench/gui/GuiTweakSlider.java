package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.gui.impl.GuiSliderSegmented;
import se.mickelus.tetra.module.data.TweakData;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GuiTweakSlider extends GuiElement {

    private GuiString labelString;
    private GuiString valueString;
    private GuiSliderSegmented slider;

    private List<String> tooltip;

    private int steps;

    public GuiTweakSlider(int x, int y, int width, TweakData tweak, Consumer<Integer> onChange) {
        super(x, y, width, 16);

        labelString = new GuiStringSmall(0, 0, I18n.format(tweak.key + ".label"));
        labelString.setAttachment(GuiAttachment.topCenter);
        addChild(labelString);

        addChild(new GuiStringSmall(-2, 1, I18n.format(tweak.key + ".left")).setAttachment(GuiAttachment.bottomLeft));
        addChild(new GuiStringSmall(-1, 1, I18n.format(tweak.key + ".right")).setAttachment(GuiAttachment.bottomRight));

        valueString = new GuiStringSmall(0, 0, "");
        valueString.setAttachment(GuiAttachment.bottomCenter);
        addChild(valueString);

        slider = new GuiSliderSegmented(-2, 3, width, tweak.steps * 2 + 1, step -> {
            valueString.setString("" + (step - tweak.steps));
            onChange.accept(step - tweak.steps);
        });
        slider.setAttachment(GuiAttachment.topCenter);
        addChild(slider);

        steps = tweak.steps;

        tooltip = Collections.singletonList(I18n.format(tweak.key + ".tooltip"));
    }

    public void setValue(int value) {
        valueString.setString("" + (value));
        slider.setValue(value + steps);
    }

    @Override
    public List<String> getTooltipLines() {
        if (labelString.hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
