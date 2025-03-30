package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.ColorHelper;
import se.mickelus.mutil.gui.GuiClickable;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.blocks.workbench.gui.SchematicRequirementGui;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class HoloSchematicListItemGui extends GuiClickable {
    private final GuiString label;
    private final SchematicRarity rarity;
    private GuiTexture border;
    private GuiTexture glyph;

    public HoloSchematicListItemGui(int x, int y, UpgradeSchematic schematic, Runnable onClickHandler) {
        this(x, y, 109, schematic, onClickHandler);
    }

    public HoloSchematicListItemGui(int x, int y, int width, UpgradeSchematic schematic, Runnable onClickHandler) {
        super(x, y, width, 14, onClickHandler);

        rarity = schematic.getRarity();

        label = new GuiString(16, 3, schematic.getName());
        label.setColor(rarity.tint);
        addChild(label);

        GlyphData glyphData = schematic.getGlyph();
        if (schematic.getType() == SchematicType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            addChild(new GuiTexture(0, 1, 16, 11, 52, 2, GuiTextures.workbench).setColor(0));
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
            border.setColor(ColorHelper.withBrightness(rarity.tint, 0.3));
            addChild(border);
        }

        glyph.setColor(rarity.tint);
        addChild(glyph);

        if (schematic.getType() == SchematicType.improvement) {
            addChild(new GuiTexture(7, 7, 7, 7, 68, 16, GuiTextures.workbench).setColor(GuiColors.muted));
        }

        addChild(new SchematicRequirementGui(label.getWidth() + 19, 2).update(schematic));

        // uncomment to highlight full size of list item
//        addChild(new GuiRect(0, 0, width - 1, 13, GuiColors.hover));
    }

    @Override
    protected void onFocus() {
        if (border != null) {
            border.setColor(ColorHelper.withBrightness(rarity.tint, 0.6));
        }
        label.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        if (border != null) {
            border.setColor(ColorHelper.withBrightness(rarity.tint, 0.3));
        }
        label.setColor(rarity.tint);
    }

    @Override
    public List<Component> getTooltipLines() {
        return super.getTooltipLines();
    }
}
