package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.List;

public class SchematicRequirementGui extends GuiElement {
    private List<Component> tooltip;

    public SchematicRequirementGui(int x, int y) {
        super(x, y, 9, 9);
        addChild(new GuiTexture(0, 0, 9, 9, 224, 32, GuiTextures.workbench));
    }


    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }

    public SchematicRequirementGui update(UpgradeSchematic schematic) {
        tooltip = schematic.getRequirementDescription();
        setVisible(tooltip != null);

        return this;
    }

    public List<Component> getTooltip() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }
}
