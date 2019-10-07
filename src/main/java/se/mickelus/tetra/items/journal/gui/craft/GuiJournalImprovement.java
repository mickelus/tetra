package se.mickelus.tetra.items.journal.gui.craft;

import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.Arrays;
import java.util.List;

public class GuiJournalImprovement extends GuiElement {

    private final static String texture = "textures/gui/workbench.png";
    private final GuiTexture backdrop;
    private final GuiString label;

    private UpgradeSchema schema;

    public GuiJournalImprovement(int x, int y, UpgradeSchema schema) {
        super(x, y, 52, 16);

        this.schema = schema;

        backdrop = new GuiTexture(1, 3, 16, 9, 52, 3, texture);
        addChild(backdrop);
        addChild(new GuiTexture(7, 8, 7, 7, 68, 16, texture));

        addChild(new GuiModuleGlyph(0, 0, 16, 16, schema.getGlyph()).setShift(false));

        label = new GuiString(20, 2, schema.getName().substring(0, 4));
        addChild(label);
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        backdrop.setColor(GuiColors.hover);
        label.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        backdrop.setColor(GuiColors.normal);
        label.setColor(GuiColors.normal);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return Arrays.asList(schema.getName(), TextFormatting.GRAY + schema.getDescription(null));
        }
        return null;
    }
}
