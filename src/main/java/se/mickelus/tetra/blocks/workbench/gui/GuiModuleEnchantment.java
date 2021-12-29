package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GuiModuleEnchantment extends GuiElement {

    private final List<String> tooltipLines;

    private Runnable hoverHandler;
    private Runnable blurHandler;

    public GuiModuleEnchantment(int x, int y, Enchantment enchantment, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 5, 4);

        addChild(new GuiRect(0, 1, width, 1, color));

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(ChatFormatting.DARK_RED + "-" + TetraEnchantmentHelper.getEnchantmentName(enchantment, 0));
        } else {
            tooltipLines.add(TetraEnchantmentHelper.getEnchantmentName(enchantment, level));
        }

        Optional.ofNullable(TetraEnchantmentHelper.getEnchantmentDescription(enchantment))
                .map(description -> description.split("\\\\n"))
                .stream()
                .flatMap(Arrays::stream)
                .map(line -> line.replace(ChatFormatting.RESET.toString(), ChatFormatting.DARK_GRAY.toString()))
                .map(line -> ChatFormatting.DARK_GRAY + line)
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
