package se.mickelus.tetra.gui.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.gui.impl.statbar.GuiBar;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GuiMagicUsage extends GuiElement {
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<String> tooltip;

    public GuiMagicUsage(int x, int y, int barLength) {
        super(x, y, barLength, 12);

        addChild(new GuiStringSmall(0, 0, I18n.format("item.modular.magic_capacity.label")));

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

            bar.setMax(Math.max(max + diffMax, max));

            tooltip = Collections.singletonList(I18n.format("item.modular.magic_capacity.description", max, value + diffValue));

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

            tooltip = Collections.singletonList(I18n.format("item.modular.magic_capacity.description", max, value));
            valueString.setString(String.format("%d/%d", max - value, max));

            bar.setMax(max);
            bar.setValue(max - value, max - value);
        }
    }

    private static int getGain(ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getMagicCapacityGain(itemStack))
                .orElse(0);
    }

    private static int getCost(ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getMagicCapacityCost(itemStack))
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
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
