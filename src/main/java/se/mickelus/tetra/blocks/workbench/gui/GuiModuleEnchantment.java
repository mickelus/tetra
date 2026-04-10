package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiModuleEnchantment extends GuiElement {

    private final List<Component> tooltipLines;

    private Runnable hoverHandler;
    private Runnable blurHandler;

    private int color;
    private GuiTexture texture;

    public GuiModuleEnchantment(int x, int y, Holder<Enchantment> enchantment, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 5, 4);

        this.color = color;
        texture = new GuiTexture(0, 0, 5, 4, 68, 27, GuiTextures.workbench).setColor(color);
        addChild(texture);

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
        texture.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();
        blurHandler.run();
        texture.setColor(color);
    }
}
