package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class GuiModuleImprovement extends GuiElement {

    private final List<Component> tooltipLines;

    private final Runnable hoverHandler;
    private final Runnable blurHandler;

    public GuiModuleImprovement(int x, int y, String improvement, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 4, 4);

        addChild(new GuiRect(0, 1, width, 1, color));

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(new TextComponent("-" + IModularItem.getImprovementName(improvement, 0)).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltipLines.add(new TextComponent(IModularItem.getImprovementName(improvement, level)));
        }

        Arrays.stream(IModularItem.getImprovementDescription(improvement).split("\\\\n"))
                .map(line -> new TextComponent(line).withStyle(ChatFormatting.DARK_GRAY))
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
    }

    @Override
    protected void onBlur() {
        super.onBlur();
        blurHandler.run();
    }
}
