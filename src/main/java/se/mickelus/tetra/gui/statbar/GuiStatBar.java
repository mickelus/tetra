package se.mickelus.tetra.gui.statbar;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.gui.statbar.getter.ILabelGetter;
import se.mickelus.tetra.gui.statbar.getter.IStatGetter;
import se.mickelus.tetra.gui.statbar.getter.ITooltipGetter;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuiStatBar extends GuiStatBase {
    protected double min;
    protected double max;

    protected GuiString labelString;
    protected GuiString valueString;
    protected GuiBar bar;

    protected GuiHorizontalLayoutGroup indicatorGroup;
    protected GuiStatIndicator[] indicators = new GuiStatIndicator[0];

    protected List<String> tooltip;
    protected List<String> extendedTooltip;

    protected GuiAlignment alignment = GuiAlignment.left;

    protected boolean inverted;

    protected IStatGetter statGetter;
    protected ILabelGetter labelGetter;
    protected ITooltipGetter tooltipGetter;

    public GuiStatBar(int x, int y, int barLength, String label, double min, double max, boolean segmented,
            IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        this(x, y, barLength, label, min, max, segmented, false, false, statGetter, labelGetter, tooltipGetter);
    }

    public GuiStatBar(int x, int y, int barLength, String label, double min, double max, boolean segmented, boolean split,
            boolean inverted, IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        super(x, y, barLength, 12);

        this.min = min;
        this.max = max;

        labelString = new GuiStringSmall(0, 0, label);
        valueString = new GuiStringSmall(0, 0, label);

        if (segmented) {
            bar = new GuiBarSegmented(0, 0, barLength + 1, min, max, inverted);
        } else {
            if (split) {
                bar = new GuiBarSplit(0, 0, barLength, max, inverted);
            } else {
                bar = new GuiBar(0, 0, barLength, min, max, inverted);
            }
        }

        indicatorGroup = new GuiHorizontalLayoutGroup(0, -1, 7, 1);

        addChild(labelString);
        addChild(valueString);
        addChild(bar);
        addChild(indicatorGroup);

        this.statGetter = statGetter;
        this.labelGetter = labelGetter;
        this.tooltipGetter = tooltipGetter;

        this.inverted = inverted;
    }

    public GuiStatBar setIndicators(GuiStatIndicator ... indicators) {
        this.indicators = indicators;
        return this;
    }

    public void setAlignment(GuiAlignment alignment) {
        this.alignment = alignment;
        realign();
    }

    protected void realign() {
        bar.setAlignment(alignment);

        labelString.setAttachment(alignment.toAttachment());
        valueString.setAttachment(alignment.toAttachment().flipHorizontal());

        indicatorGroup.setAttachment(alignment.toAttachment());
        int offset = labelString.getWidth() + 2;
        indicatorGroup.setX(GuiAlignment.right.equals(alignment) ? -offset : offset);
    }

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        double value;
        double diffValue;

        if (!previewStack.isEmpty()) {
            value = statGetter.getValue(player, currentStack);
            diffValue = statGetter.getValue(player, previewStack);

            tooltip = Collections.singletonList(getCombinedTooltip(player, previewStack));
            extendedTooltip = Collections.singletonList(getCombinedTooltipExtended(player, previewStack));
        } else {
            value = statGetter.getValue(player, currentStack);

            if (slot != null) {
                diffValue = value;
                value = value - getSlotValue(player, currentStack, slot, improvement);
            } else {
                diffValue = value;
            }

            tooltip = Collections.singletonList(getCombinedTooltip(player, currentStack));
            extendedTooltip = Collections.singletonList(getCombinedTooltipExtended(player, currentStack));
        }

        updateValue(value, diffValue);

        updateIndicators(player, currentStack, previewStack, slot, improvement);
    }

    protected void updateIndicators(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        indicatorGroup.clearChildren();

        for (int i = 0; i < indicators.length; i++) {
            if (indicators[i].update(player, currentStack, previewStack, slot, improvement)) {
                indicatorGroup.addChild(indicators[i]);
            }
        }
    }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        return statGetter.shouldShow(player, currentStack, previewStack);
    }

    protected double getSlotValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
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
            if (Screen.hasShiftDown()) {
                return extendedTooltip;
            }
            return tooltip;
        }
        return super.getTooltipLines();
    }

    protected List<GuiStatIndicator> getActiveIndicators() {
        return indicatorGroup.getChildren(GuiStatIndicator.class);
    }

    protected String getCombinedTooltipBase(PlayerEntity player, ItemStack itemStack) {
        String tooltip = tooltipGetter.getTooltipBase(player, itemStack);

        tooltip += getActiveIndicators().stream()
                .map(indicator -> TextFormatting.YELLOW + indicator.getLabel() + "\n" + TextFormatting.GRAY + indicator.getTooltipBase(player, itemStack))
                .map(string -> "\n \n" + string)
                .collect(Collectors.joining())
                .replace(TextFormatting.RESET.toString(), TextFormatting.GRAY.toString());

        return tooltip;
    }

    protected String getCombinedTooltip(PlayerEntity player, ItemStack itemStack) {
        String tooltip = getCombinedTooltipBase(player, itemStack);

        if (tooltipGetter.hasExtendedTooltip(player, itemStack) || getActiveIndicators().stream().anyMatch(ind -> ind.hasExtendedTooltip(player, itemStack))) {
            tooltip += "\n \n" + Tooltips.expand.getString();
        }

        return tooltip;
    }

    protected String getCombinedTooltipExtended(PlayerEntity player, ItemStack itemStack) {
        String tooltip = getCombinedTooltipBase(player, itemStack);

        if (tooltipGetter.hasExtendedTooltip(player, itemStack) || getActiveIndicators().stream().anyMatch(ind -> ind.hasExtendedTooltip(player, itemStack))) {
            tooltip += "\n \n" + Tooltips.expanded.getString();

            if (tooltipGetter.hasExtendedTooltip(player, itemStack)) {
                tooltip += "\n" + TextFormatting.GRAY + tooltipGetter.getTooltipExtension(player, itemStack)
                        .replace(TextFormatting.RESET.toString(), TextFormatting.GRAY.toString());
            }

            tooltip += getActiveIndicators().stream()
                    .filter(indicator -> indicator.hasExtendedTooltip(player, itemStack))
                    .map(indicator -> TextFormatting.GRAY + indicator.getTooltipExtension(player, itemStack))
                    .map(string -> string.replace(TextFormatting.RESET.toString(), TextFormatting.GRAY.toString()))
                    .map(string -> "\n" + string)
                    .collect(Collectors.joining());
        }

        return tooltip;
    }
}
