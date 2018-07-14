package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRect;

import java.util.Arrays;
import java.util.List;

public class GuiModuleImprovement extends GuiElement {

    private final List<String> tooltipLines;

    public GuiModuleImprovement(int x, int y, String improvement, int level, int color) {
        super(x, y, 4, 3);

        addChild(new GuiRect(0, 1, width, 1, color));

        if (level < 0) {
            tooltipLines = Arrays.asList(
                    ChatFormatting.DARK_RED + "-" + I18n.format(improvement + ".name", ""),
                    ChatFormatting.GRAY + I18n.format(improvement + ".description"));
        } else if (level == 0) {
            tooltipLines = Arrays.asList(
                    I18n.format(improvement + ".name"),
                    ChatFormatting.GRAY + I18n.format(improvement + ".description"));
        } else {
            tooltipLines = Arrays.asList(
                    I18n.format(improvement + ".name") + " " + I18n.format("enchantment.level." + level),
                    ChatFormatting.GRAY + I18n.format(improvement + ".description"));
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltipLines;
        }
        return null;
    }
}
