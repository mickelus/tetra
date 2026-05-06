package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiModuleImprovement extends GuiElement {

    private final List<Component> tooltipLines;

    private final Runnable hoverHandler;
    private final Runnable blurHandler;

    private final int color;
    private final GuiTexture texture;

    public GuiModuleImprovement(int x, int y, String improvement, int level, int color, ItemStack itemStack, Runnable hoverHandler,
            Runnable blurHandler) {
        super(x, y, 5, 4);

        this.color = color;
        texture = new GuiTexture(0, 0, 5, 4, 68, 23, GuiTextures.workbench).setColor(color);
        addChild(texture);

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(Component.literal("-" + IModularItem.getImprovementName(improvement, level, itemStack)).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltipLines.add(Component.literal(IModularItem.getImprovementName(improvement, level, itemStack)));
        }

        Arrays.stream(IModularItem.getImprovementDescription(improvement, level, itemStack).split("\\\\n"))
                .map(line -> Component.literal(line).withStyle(ChatFormatting.DARK_GRAY))
                .forEachOrdered(tooltipLines::add);

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
