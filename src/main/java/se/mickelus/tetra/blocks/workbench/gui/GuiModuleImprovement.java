package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiModuleImprovement extends GuiElement {

    private final List<String> tooltipLines;

    private Runnable hoverHandler;
    private Runnable blurHandler;

    public GuiModuleImprovement(int x, int y, String improvement, int level, int color, Runnable hoverHandler, Runnable blurHandler) {
        super(x, y, 4, 4);

        addChild(new GuiRect(0, 1, width, 1, color));

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(ChatFormatting.DARK_RED + "-" + IModularItem.getImprovementName(improvement, 0));
        } else {
            tooltipLines.add(IModularItem.getImprovementName(improvement, level));
        }

        Arrays.stream(IModularItem.getImprovementDescription(improvement).split("\\\\n"))
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
