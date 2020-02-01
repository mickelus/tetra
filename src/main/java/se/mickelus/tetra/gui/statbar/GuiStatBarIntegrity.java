package se.mickelus.tetra.gui.statbar;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collections;
import java.util.List;

public class GuiStatBarIntegrity extends GuiStatBase {
    protected double max = 9;

    protected GuiString labelString;
    protected GuiString valueString;

    protected GuiBar barPositive;
    protected GuiBar barNegative;

    protected List<String> tooltip;

    protected IStatGetter statGetter;
    protected ILabelGetter labelGetter;

    protected ITooltipGetter tooltipGetterPositive;
    protected ITooltipGetter tooltipGetterNegative;

    public GuiStatBarIntegrity(int x, int y) {
        super(x, y, GuiStats.barLength, 12);

        labelString = new GuiStringSmall(0, 0, I18n.format("stats.integrity"));

        valueString = new GuiStringSmall(0, 0, "");
        valueString.setAttachment(GuiAttachment.topRight);

        barNegative = new GuiBarSegmented(-1, 0, 27, 0, max, true);
        barNegative.setAlignment(GuiAlignment.right);
        addChild(barNegative);

        barPositive = new GuiBarSegmented(1, 0, 27, 0, max);
        barPositive.setAttachment(GuiAttachment.topRight);
        addChild(barPositive);

        addChild(labelString);
        addChild(valueString);

        addChild(new GuiRect(29, 5, 1, 3, GuiColors.muted));

        statGetter = new StatGetterIntegrity();
        labelGetter = LabelGetterBasic.integerLabel;
        tooltipGetterPositive = new TooltipGetterInteger("stats.integrity.tooltip_positive", statGetter);
        tooltipGetterNegative = new TooltipGetterInteger("stats.integrity.tooltip_negative", statGetter, true);
    }

    @Override
    public void setAlignment(GuiAlignment alignment) {}

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        double value;
        double diffValue;

        if (!previewStack.isEmpty()) {
            value = statGetter.getValue(player, currentStack);
            diffValue = statGetter.getValue(player, previewStack);
        } else {
            value = statGetter.getValue(player, currentStack);

            if (slot != null) {
                diffValue = value;
                value = value - getSlotValue(player, currentStack, slot, improvement);
            } else {
                diffValue = value;
            }
        }

        if (value < 0) {
            tooltip = Collections.singletonList(tooltipGetterNegative.getTooltip(player, previewStack));
        } else {
            tooltip = Collections.singletonList(tooltipGetterPositive.getTooltip(player, previewStack));
        }

        updateValue(value, diffValue);
    }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        return statGetter.shouldShow(player, currentStack, previewStack);
    }

    protected double getSlotValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> {
                    if (improvement != null) {
                        return statGetter.getValue(player, itemStack, slot, improvement);
                    }

                    return statGetter.getValue(player, itemStack, slot);
                })
                .orElse(0d);
    }

    public void updateValue(double value, double diffValue) {
        barNegative.setValue(value > 0 ? 0 : -value, diffValue > 0 ? 0 : -diffValue);
        barPositive.setValue(value < 0 ? 0 : value, diffValue < 0 ? 0 : diffValue);

        updateValueLabel(value, diffValue);
    }

    private void updateValueLabel(double value, double diffValue) {
        valueString.setString(labelGetter.getLabel(value, diffValue, false));
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
