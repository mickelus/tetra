package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schema.SchemaRarity;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.Collections;
import java.util.List;

public class GuiSchemaListItem extends GuiClickable {

    private GuiTexture border;
    private GuiTexture glyph;
    private GuiString label;

    private SchemaRarity rarity;

    public GuiSchemaListItem(int x, int y, UpgradeSchema schema, Runnable onClickHandler) {
        super(x, y, 109, 14, onClickHandler);

        rarity = schema.getRarity();

        label = new GuiString(16, 3, 93, schema.getName());
        addChild(label);

        GlyphData glyphData = schema.getGlyph();
        if (schema.getType() == SchemaType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, "textures/gui/workbench.png");
            glyph = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schema.getType() == SchemaType.minor) {
            border = new GuiTexture(2, 1, 11, 11, 68, 0, "textures/gui/workbench.png");
            glyph = new GuiTexture(4, 3, 8, 8, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schema.getType() == SchemaType.improvement) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, "textures/gui/workbench.png");
            glyph = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schema.getType() == SchemaType.other) {
            glyph = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        }

        if (border != null) {
            border.setOpacity(0.3f);
            border.setColor(rarity.tint);
            addChild(border);
        }

        glyph.setColor(rarity.tint);
        addChild(glyph);

        if (schema.getType() == SchemaType.improvement) {
            addChild(new GuiTexture(7, 7, 7, 7, 68, 16, "textures/gui/workbench.png"));
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
        label.setColor(GuiColors.normal);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus() && rarity.equals(SchemaRarity.temporary)) {
            return Collections.singletonList(I18n.format("workbench.schema_list.temporary"));
        }

        return Collections.emptyList();
    }
}
