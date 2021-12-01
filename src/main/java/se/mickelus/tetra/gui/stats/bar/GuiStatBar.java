package se.mickelus.tetra.gui.stats.bar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class GuiStatBar extends GuiStatBase {
    protected double min;
    protected double max;

    protected String labelKey;
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

    public GuiStatBar(int x, int y, int barLength, String labelKey, double min, double max, boolean segmented,
            IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        this(x, y, barLength, labelKey, min, max, segmented, false, false, statGetter, labelGetter, tooltipGetter);
    }

    public GuiStatBar(int x, int y, int barLength, String labelKey, double min, double max, boolean segmented, boolean split,
            boolean inverted, IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        super(x, y, barLength, 12);

        this.min = min;
        this.max = max;

        this.labelKey = labelKey;

        labelString = new GuiStringSmall(0, 0, "");
        valueString = new GuiStringSmall(0, 0, "");

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
    public void update(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        double value;
        double diffValue;

        if (labelKey != null) {
            labelString.setString(I18n.get(labelKey));
        }
        labelString.setVisible(labelKey != null);

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

    protected void updateIndicators(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        indicatorGroup.clearChildren();

        for (int i = 0; i < indicators.length; i++) {
            if (indicators[i].update(player, currentStack, previewStack, slot, improvement)) {
                indicatorGroup.addChild(indicators[i]);
            }
        }
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        return statGetter.shouldShow(player, currentStack, previewStack);
    }

    protected double getSlotValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
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

    protected String getCombinedTooltipBase(Player player, ItemStack itemStack) {
        String tooltip = tooltipGetter.getTooltipBase(player, itemStack);

        tooltip += getActiveIndicators().stream()
                .filter(indicator -> indicator.isActive(player, itemStack))
                .map(indicator -> ChatFormatting.YELLOW + indicator.getLabel() + "\n" + ChatFormatting.GRAY + indicator.getTooltipBase(player, itemStack))
                .map(string -> "\n \n" + string)
                .collect(Collectors.joining())
                .replace(ChatFormatting.RESET.toString(), ChatFormatting.GRAY.toString());

        return tooltip;
    }

    protected String getCombinedTooltip(Player player, ItemStack itemStack) {
        String tooltip = getCombinedTooltipBase(player, itemStack);

        if (tooltipGetter.hasExtendedTooltip(player, itemStack) || getActiveIndicators().stream().anyMatch(ind -> ind.hasExtendedTooltip(player, itemStack))) {
            tooltip += "\n \n" + Tooltips.expand.getString();
        }

        return tooltip;
    }

    protected String getCombinedTooltipExtended(Player player, ItemStack itemStack) {
        String tooltip = getCombinedTooltipBase(player, itemStack);

        if (tooltipGetter.hasExtendedTooltip(player, itemStack) || getActiveIndicators().stream().anyMatch(ind -> ind.hasExtendedTooltip(player, itemStack))) {
            tooltip += "\n \n" + Tooltips.expanded.getString() + "\n";

            List<String> extendedTooltip = new LinkedList<>();
            if (tooltipGetter.hasExtendedTooltip(player, itemStack)) {
                extendedTooltip.add(ChatFormatting.GRAY + tooltipGetter.getTooltipExtension(player, itemStack)
                        .replace(ChatFormatting.RESET.toString(), ChatFormatting.GRAY.toString()));
            }

            getActiveIndicators().stream()
                    .filter(indicator -> indicator.hasExtendedTooltip(player, itemStack))
                    .map(indicator -> ChatFormatting.GRAY + indicator.getTooltipExtension(player, itemStack))
                    .map(string -> string.replace(ChatFormatting.RESET.toString(), ChatFormatting.GRAY.toString()))
                    .map(string -> "\n" + string)
                    .forEach(extendedTooltip::add);

            tooltip += String.join("\n \n", extendedTooltip);
        }

        return tooltip;
    }
}
