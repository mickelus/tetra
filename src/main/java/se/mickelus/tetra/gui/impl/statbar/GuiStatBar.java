package se.mickelus.tetra.gui.impl.statbar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.impl.statbar.getter.ILabelGetter;
import se.mickelus.tetra.gui.impl.statbar.getter.IStatGetter;
import se.mickelus.tetra.gui.impl.statbar.getter.ITooltipGetter;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collections;
import java.util.List;

public class GuiStatBar extends GuiStatBase {
    protected double min;
    protected double max;

    protected GuiString labelString;
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<String> tooltip;

    protected GuiAlignment alignment = GuiAlignment.left;

    protected IStatGetter statGetter;
    protected ILabelGetter labelGetter;
    protected ITooltipGetter tooltipGetter;

    public GuiStatBar(int x, int y, int barLength, String label, double min, double max, boolean segmented,
            IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        this(x, y, barLength, label, min, max, segmented, false, statGetter, labelGetter, tooltipGetter);
    }

    public GuiStatBar(int x, int y, int barLength, String label, double min, double max, boolean segmented, boolean split,
            IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        super(x, y, barLength, 12);

        this.min = min;
        this.max = max;

        labelString = new GuiStringSmall(0, 0, label);
        valueString = new GuiStringSmall(0, 0, label);

        if (segmented) {
            bar = new GuiBarSegmented(0, 0, barLength + 1, min, max);
        } else {
            if (split) {
                bar = new GuiBarSplit(0, 0, barLength, max);
            } else {
                bar = new GuiBar(0, 0, barLength, min, max);
            }
        }

        addChild(labelString);
        addChild(valueString);
        addChild(bar);

        this.statGetter = statGetter;
        this.labelGetter = labelGetter;
        this.tooltipGetter = tooltipGetter;
    }

    public void setAlignment(GuiAlignment alignment) {
        this.alignment = alignment;
        realign();
    }

    protected void realign() {
        bar.setAlignment(alignment);

        labelString.setAttachment(alignment.toAttachment());
        valueString.setAttachment(alignment.toAttachment().flipHorizontal());
    }

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        double value;
        double diffValue;

        if (!previewStack.isEmpty()) {
            value = statGetter.getValue(player, currentStack);
            diffValue = statGetter.getValue(player, previewStack);

            tooltip = Collections.singletonList(tooltipGetter.getTooltip(player, previewStack));
        } else {
            value = statGetter.getValue(player, currentStack);

            if (slot != null) {
                diffValue = value;
                value = value - getSlotValue(player, currentStack, slot, improvement);
            } else {
                diffValue = value;
            }

            tooltip = Collections.singletonList(tooltipGetter.getTooltip(player, currentStack));
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
        bar.setValue(value, diffValue);
        updateValueLabel(value, diffValue);
    }

    private void updateValueLabel(double value, double diffValue) {
        valueString.setString(labelGetter.getLabel(value, diffValue, alignment == GuiAlignment.right));
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
