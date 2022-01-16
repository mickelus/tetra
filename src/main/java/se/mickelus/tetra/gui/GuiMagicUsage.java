package se.mickelus.tetra.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiStringSmall;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.gui.stats.bar.GuiBar;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class GuiMagicUsage extends GuiElement {
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<Component> tooltip;
    protected List<Component> tooltipExtended;

    public GuiMagicUsage(int x, int y, int barLength) {
        super(x, y, barLength, 12);

        addChild(new GuiStringSmall(0, 0, I18n.get("item.tetra.modular.magic_capacity.label")));

        valueString = new GuiStringSmall(0, 0, "");
        valueString.setAttachment(GuiAttachment.topRight);
        addChild(valueString);

        bar = new GuiBar(0, 0, barLength, 0, 0);
        addChild(bar);

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
                    new TranslatableComponent("item.tetra.modular.magic_capacity.description", max, value + diffValue, xpCost, risk),
                    new TextComponent(""),
                    Tooltips.expand);

            tooltipExtended = Arrays.asList(
                    new TranslatableComponent("item.tetra.modular.magic_capacity.description", max, value + diffValue, xpCost, risk),
                    new TextComponent(""),
                    Tooltips.expanded,
                    new TranslatableComponent("item.tetra.modular.magic_capacity.description_extended"));

            if (diffMax != 0) {
                bar.setValue(max, max + diffMax);
                valueString.setString(String.format("%s(%+d)%s %d/%d", diffMax < 0 ? ChatFormatting.RED : ChatFormatting.GREEN,
                        diffMax, ChatFormatting.RESET, max + diffMax, max + diffMax));
            } else if (diffValue != 0) {
                bar.setValue(max - value, max - value - diffValue);
                valueString.setString(String.format("%s(%+d)%s %d/%d", diffValue > 0 ? ChatFormatting.RED : ChatFormatting.GREEN,
                        -diffValue, ChatFormatting.RESET, max - value - diffValue, max));
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
                    new TranslatableComponent("item.tetra.modular.magic_capacity.description", max, value, xpCost, risk),
                    new TextComponent(""),
                    Tooltips.expand);

            tooltipExtended = Arrays.asList(
                    new TranslatableComponent("item.tetra.modular.magic_capacity.description", max, value, xpCost, risk),
                    new TextComponent(""),
                    Tooltips.expanded,
                    new TranslatableComponent("item.tetra.modular.magic_capacity.description_extended"));
            valueString.setString(String.format("%d/%d", max - value, max));

            bar.setMax(max);
            bar.setValue(max - value, max - value);
        }
    }

    public boolean hasChanged(ItemStack itemStack, ItemStack previewStack, String slot) {
        return !previewStack.isEmpty() && (getCost(itemStack, slot) != getCost(previewStack, slot) || getGain(itemStack, slot) != getGain(previewStack, slot));
    }

    public boolean providesCapacity(ItemStack itemStack, ItemStack previewStack, String slot) {
        return getGain(itemStack, slot) > 0 || getGain(previewStack, slot) > 0;
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            if (Screen.hasShiftDown()) {
                return tooltipExtended;
            }

            return tooltip;
        }
        return super.getTooltipLines();
    }
}
