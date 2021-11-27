package se.mickelus.tetra.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.gui.stats.bar.GuiBar;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.List;

public class GuiMagicUsage extends GuiElement {
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<String> tooltip;
    protected List<String> tooltipExtended;

    public GuiMagicUsage(int x, int y, int barLength) {
        super(x, y, barLength, 12);

        addChild(new GuiStringSmall(0, 0, I18n.format("item.tetra.modular.magic_capacity.label")));

        valueString = new GuiStringSmall(0, 0, "");
        valueString.setAttachment(GuiAttachment.topRight);
        addChild(valueString);

        bar = new GuiBar(0, 0, barLength, 0, 0);
        addChild(bar);

    }

    public void update(ItemStack itemStack, ItemStack previewStack, String slot) {
        if (!previewStack.isEmpty()) {
            int value = getCost(itemStack, slot);
            int diffValue = getCost(previewStack, slot) - value;

            int max = getGain(itemStack, slot);
            int diffMax = getGain(previewStack, slot) - max;

            int risk = Math.round(getDestabilizeChance(previewStack, slot) * 100);
            int xpCost = getExperienceCost(previewStack, slot);

            bar.setMax(Math.max(max + diffMax, max));

            tooltip = Arrays.asList(
                    I18n.format("item.tetra.modular.magic_capacity.description", max, value + diffValue, xpCost, risk),
                    " ",
                    Tooltips.expand.getString());

            tooltipExtended = Arrays.asList(
                    I18n.format("item.tetra.modular.magic_capacity.description", max, value + diffValue, xpCost, risk),
                    " ",
                    Tooltips.expanded.getString(),
                    I18n.format("item.tetra.modular.magic_capacity.description_extended"));

            if (diffMax != 0) {
                bar.setValue(max, max + diffMax);
                valueString.setString(String.format("%s(%+d)%s %d/%d", diffMax < 0 ? TextFormatting.RED : TextFormatting.GREEN,
                        diffMax, TextFormatting.RESET, max + diffMax, max + diffMax));
            } else if (diffValue != 0) {
                bar.setValue(max - value, max - value - diffValue);
                valueString.setString(String.format("%s(%+d)%s %d/%d", diffValue > 0 ? TextFormatting.RED : TextFormatting.GREEN,
                        -diffValue, TextFormatting.RESET, max - value - diffValue, max));
            } else {
                bar.setValue(max - value, max - value);
                valueString.setString(String.format("%d/%d", max - value, max));
            }
        } else {
            int value = getCost(itemStack, slot);
            int max = getGain(itemStack, slot);

            int risk = Math.round(getDestabilizeChance(itemStack, slot) * 100);
            int xpCost = getExperienceCost(itemStack, slot);

            tooltip = Arrays.asList(
                    I18n.format("item.tetra.modular.magic_capacity.description", max, value, xpCost, risk),
                    " ",
                    Tooltips.expand.getString());

            tooltipExtended = Arrays.asList(
                    I18n.format("item.tetra.modular.magic_capacity.description", max, value, xpCost, risk),
                    " ",
                    Tooltips.expanded.getString(),
                    I18n.format("item.tetra.modular.magic_capacity.description_extended"));
            valueString.setString(String.format("%d/%d", max - value, max));

            bar.setMax(max);
            bar.setValue(max - value, max - value);
        }
    }

    private static int getGain(ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getMagicCapacityGain(itemStack))
                .orElse(0);
    }

    private static int getCost(ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getMagicCapacityCost(itemStack))
                .orElse(0);
    }

    private static float getDestabilizeChance(ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getDestabilizationChance(itemStack, 1))
                .orElse(0f);
    }

    private static int getExperienceCost(ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getRepairExperienceCost(itemStack))
                .orElse(0);
    }

    public boolean hasChanged(ItemStack itemStack, ItemStack previewStack, String slot) {
        return !previewStack.isEmpty() && (getCost(itemStack, slot) != getCost(previewStack, slot) || getGain(itemStack, slot) != getGain(previewStack, slot));
    }

    public boolean providesCapacity(ItemStack itemStack, ItemStack previewStack, String slot) {
        return getGain(itemStack, slot) > 0 || getGain(previewStack, slot) > 0;
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            if (Screen.hasShiftDown()) {
                return tooltipExtended;
            }

            return tooltip;
        }
        return super.getTooltipLines();
    }
}
