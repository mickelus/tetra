package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.List;

public class HoloImprovementGui extends GuiElement {

    private final GuiTexture backdrop;
    private final GuiString label;

    private UpgradeSchematic schematic;

    public HoloImprovementGui(int x, int y, UpgradeSchematic schematic) {
        super(x, y, 52, 16);

        this.schematic = schematic;

        backdrop = new GuiTexture(1, 3, 16, 9, 52, 3, GuiTextures.workbench);
        addChild(backdrop);

        addChild(new GuiModuleGlyph(0, 0, 16, 16, schematic.getGlyph()).setShift(false));
        addChild(new GuiTexture(7, 8, 7, 7, 68, 16, GuiTextures.workbench));

        String schematicName = schematic.getName();
        if (schematicName.length() > 4) {
            schematicName = schematicName.substring(0, 4);
        }
        label = new GuiString(20, 2, schematicName);
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
            return Arrays.asList(schematic.getName(), TextFormatting.GRAY + schematic.getDescription(null)
                    .replace("\\n", "\n")
                    .replace("\n", "\n" + TextFormatting.GRAY));
        }
        return null;
    }
}
