package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiModuleImprovement extends GuiElement {

    private final List<String> tooltipLines;

    public GuiModuleImprovement(int x, int y, String improvement, int level, int color) {
        super(x, y, 4, 3);

        addChild(new GuiRect(0, 1, width, 1, color));

        tooltipLines = new ArrayList<>();

        if (level < 0) {
            tooltipLines.add(ChatFormatting.DARK_RED + "-" + I18n.format(improvement + ".name", ""));
        } else if (level == 0) {
            tooltipLines.add(I18n.format(improvement + ".name"));
        } else {
            tooltipLines.add(I18n.format(improvement + ".name") + " " + I18n.format("enchantment.level." + level));
        }

        Arrays.stream(I18n.format(improvement + ".description").split("\\\\n"))
                .map(line -> line.replace(ChatFormatting.RESET.toString(), ChatFormatting.DARK_GRAY.toString()))
                .map(line -> ChatFormatting.DARK_GRAY + line)
                .forEachOrdered(tooltipLines::add);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltipLines;
        }
        return null;
    }
}
