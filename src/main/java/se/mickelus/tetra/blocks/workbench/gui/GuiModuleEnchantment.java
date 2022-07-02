package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiModuleEnchantment extends GuiElement {

    private final List<Component> tooltipLines;

    private Runnable hoverHandler;
    private Runnable blurHandler;

    public GuiModuleEnchantment(int x, int y, Enchantment enchantment, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 5, 4);

        addChild(new GuiRect(0, 1, width, 1, color));

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(Component.literal("-").append(TetraEnchantmentHelper.getEnchantmentName(enchantment, 0)).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltipLines.add(Component.literal(TetraEnchantmentHelper.getEnchantmentName(enchantment, level)));
        }

        Optional.ofNullable(TetraEnchantmentHelper.getEnchantmentDescription(enchantment))
                .map(description -> Component.literal(description).withStyle(ChatFormatting.DARK_GRAY))
                .ifPresent(tooltipLines::add);

        this.hoverHandler = hoverHandler;
        this.blurHandler = blurHandler;
    }

    @Override
    public List<Component> getTooltipLines() {
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
