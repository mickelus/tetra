package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.GlyphData;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiSchemaListItem extends GuiClickable {

    private GuiElement border;
    private GuiString label;

    public GuiSchemaListItem(int x, int y, UpgradeSchema schema, Runnable onClickHandler) {
        super(x, y, 109, 14, onClickHandler);

        label = new GuiString(16, 3, 93, schema.getName());
        addChild(label);

        GlyphData glyphData = schema.getGlyph();
        if (schema.getType() == SchemaType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, "textures/gui/workbench.png").setOpacity(0.3f);
            addChild(border);
            addChild(new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation));
        } else if (schema.getType() == SchemaType.minor) {
            border = new GuiTexture(2, 1, 11, 11, 68, 0, "textures/gui/workbench.png").setOpacity(0.3f);
            addChild(border);
            addChild(new GuiTexture(4, 3, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation));
        } else if (schema.getType() == SchemaType.improvement) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, "textures/gui/workbench.png").setOpacity(0.3f);
            addChild(border);
            addChild(new GuiTexture(7, 7, 7, 7, 68, 16, "textures/gui/workbench.png"));
            addChild(new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation));
        } else if (schema.getType() == SchemaType.other) {
            border = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation).setOpacity(0.3f);
            addChild(border);
        }
    }

    @Override
    protected void onFocus() {
        if (border != null) {
            border.setOpacity(0.6f);
        }
        label.setColor(0xffffcc);
    }

    @Override
    protected void onBlur() {
        if (border != null) {
            border.setOpacity(0.3f);
        }
        label.setColor(0xffffff);
    }
}
