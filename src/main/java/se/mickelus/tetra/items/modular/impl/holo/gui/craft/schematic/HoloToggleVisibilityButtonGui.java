package se.mickelus.tetra.items.modular.impl.holo.gui.craft.schematic;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiClickable;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.List;

public class HoloToggleVisibilityButtonGui extends GuiClickable {
    private GuiTexture texture;
    private List<Component> tooltip;

    public HoloToggleVisibilityButtonGui(int x, int y, Runnable onClickHandler) {
        super(x, y, 32, 12, onClickHandler);

        update(false);
    }

    public void update(boolean displayAllSchematics) {
        clearChildren();
        if (displayAllSchematics) {
            texture = new GuiTexture(-2, -2, 16, 16, 80, 0, GuiTextures.holo);
            addChild(texture);
            addChild(new GuiString(13, 0, I18n.get("tetra.holo.craft.showing_applicable")).setAttachment(GuiAttachment.middleLeft));
            tooltip = ImmutableList.of(Component.translatable("tetra.holo.craft.showing_applicable.tooltip"));
        } else {
            texture = new GuiTexture(-2, -2, 16, 16, 96, 0, GuiTextures.holo);
            addChild(texture);
            addChild(new GuiString(13, 0, I18n.get("tetra.holo.craft.showing_available")).setAttachment(GuiAttachment.middleLeft));
            tooltip = ImmutableList.of(Component.translatable("tetra.holo.craft.showing_available.tooltip"));
        }
    }

    @Override
    protected void onFocus() {
        texture.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        texture.setColor(GuiColors.normal);
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }
}
