package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.List;

public class HoloMaterialImprovementGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<String> tooltip;

    public HoloMaterialImprovementGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);

        String improvementName = I18n.format("tetra.improvement." + key + ".name");

        tooltip = ImmutableList.of(I18n.format("tetra.holo.craft.materials.stat_modifier.tooltip", improvementName),
                TextFormatting.DARK_GRAY + I18n.format("tetra.improvement." + key + ".description"));

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(0, 8, improvementName);
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);
        
        if (current != preview) {
            value.setColor(preview ? GuiColors.add : GuiColors.remove);
        }

        label = new GuiStringOutline(0, -3, I18n.format("tetra.holo.craft.materials.stat_modifier"));
        label.setColor(GuiColors.muted);
        label.setAttachment(GuiAttachment.bottomCenter);
        addChild(label);
    }

    @Override
    public List<String> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
