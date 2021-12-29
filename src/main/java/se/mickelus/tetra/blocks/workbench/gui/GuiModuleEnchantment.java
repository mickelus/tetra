package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GuiModuleEnchantment extends GuiElement {

    private final List<String> tooltipLines;

    private Runnable hoverHandler;
    private Runnable blurHandler;

    public GuiModuleEnchantment(int x, int y, Enchantment enchantment, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 5, 4);

        addChild(new GuiRect(0, 1, width, 1, color));

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(TextFormatting.DARK_RED + "-" + TetraEnchantmentHelper.getEnchantmentName(enchantment, 0));
        } else {
            tooltipLines.add(TetraEnchantmentHelper.getEnchantmentName(enchantment, level));
        }

        Optional.ofNullable(TetraEnchantmentHelper.getEnchantmentDescription(enchantment))
                .map(description -> description.split("\\\\n"))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(line -> line.replace(TextFormatting.RESET.toString(), TextFormatting.DARK_GRAY.toString()))
                .map(line -> TextFormatting.DARK_GRAY + line)
                .forEachOrdered(tooltipLines::add);

        this.hoverHandler = hoverHandler;
        this.blurHandler = blurHandler;
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltipLines;
        }
        return null;
    }

    @Override
    protected void onFocus() {
        super.onFocus();
        hoverHandler.run();
    }

    @Override
    protected void onBlur() {
        super.onBlur();
        blurHandler.run();
    }
}
