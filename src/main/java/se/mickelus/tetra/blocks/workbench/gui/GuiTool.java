package se.mickelus.tetra.blocks.workbench.gui;

import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.client.ItemAbilityIconStore;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.GlyphData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class GuiTool extends GuiElement {
    public static final int width = 16;
    private final GuiString levelIndicator;

    protected GuiElement iconContainer;
    protected ItemAbility toolAction;

    private final GlyphData fallback = new GlyphData(GuiTextures.toolActions, 240, 0);

    public GuiTool(int x, int y, ItemAbility toolAction) {
        super(x, y, width, 16);
        this.toolAction = toolAction;

        iconContainer = new GuiElement(0, 0, 16, 16);
        addChild(iconContainer);

        updateIcon();

        levelIndicator = new GuiStringOutline(10, 8, "");
        addChild(levelIndicator);
    }

    public void update(int level, int color) {
        levelIndicator.setVisible(level >= 0);
        levelIndicator.setString(level + "");
        levelIndicator.setColor(color);

        updateIcon();
    }

    protected void updateIcon() {
        iconContainer.clearChildren();
        GlyphData glyph = Optional.ofNullable(ItemAbilityIconStore.instance.getIcon(toolAction)).orElse(fallback);
        iconContainer.addChild(new GuiTexture(0, 0, 16, 16, glyph.textureX, glyph.textureY, glyph.textureLocation));
    }

    public ItemAbility getItemAbility() {
        return toolAction;
    }
}
