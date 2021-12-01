package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.mgui.gui.GuiClickable;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Collections;
import java.util.List;

public class GuiSchematicListItem extends GuiClickable {

    private GuiTexture border;
    private GuiTexture glyph;
    private GuiString label;

    private SchematicRarity rarity;

    public GuiSchematicListItem(int x, int y, UpgradeSchematic schematic, Runnable onClickHandler) {
        this(x, y, 109, schematic, onClickHandler);
    }

    public GuiSchematicListItem(int x, int y, int width, UpgradeSchematic schematic, Runnable onClickHandler) {
        super(x, y, width, 14, onClickHandler);

        rarity = schematic.getRarity();

        label = new GuiString(16, 3, width - 16, schematic.getName());
        label.setColor(rarity.tint);
        addChild(label);

        GlyphData glyphData = schematic.getGlyph();
        if (schematic.getType() == SchematicType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            glyph = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.minor) {
            border = new GuiTexture(2, 1, 11, 11, 68, 0, GuiTextures.workbench);
            glyph = new GuiTexture(4, 3, 8, 8, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.improvement) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            glyph = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.other) {
            glyph = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        }

        if (border != null) {
            border.setOpacity(0.3f);
            border.setColor(rarity.tint);
            addChild(border);
        }

        glyph.setColor(rarity.tint);
        addChild(glyph);

        if (schematic.getType() == SchematicType.improvement) {
            addChild(new GuiTexture(7, 7, 7, 7, 68, 16, GuiTextures.workbench).setColor(GuiColors.muted));
        }
    }

    @Override
    protected void onFocus() {
        if (border != null) {
            border.setOpacity(0.6f);
        }
        label.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        if (border != null) {
            border.setOpacity(0.3f);
        }
        label.setColor(rarity.tint);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus() && rarity.equals(SchematicRarity.temporary)) {
            return Collections.singletonList(I18n.get("tetra.workbench.schematic_list.temporary"));
        }

        return null;
    }
}
