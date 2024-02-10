package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

    private int color;
    private GuiTexture texture;

    public GuiModuleImprovement(int x, int y, String improvement, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 5, 4);

        this.color = color;
        texture = new GuiTexture(0, 0, 5, 4, 68, 23, GuiTextures.workbench).setColor(color);
        addChild(texture);

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(Component.literal("-" + IModularItem.getImprovementName(improvement, 0)).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltipLines.add(Component.literal(IModularItem.getImprovementName(improvement, level)));
        }

        Arrays.stream(IModularItem.getImprovementDescription(improvement).split("\\\\n"))
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
