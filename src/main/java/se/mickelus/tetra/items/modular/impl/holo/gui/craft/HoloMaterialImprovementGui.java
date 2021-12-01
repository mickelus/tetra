package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.List;

public class HoloMaterialImprovementGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<String> tooltip;

    public HoloMaterialImprovementGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);

        String improvementName = IModularItem.getImprovementName(key, 0);

        tooltip = ImmutableList.of(I18n.get("tetra.holo.craft.materials.stat_modifier.tooltip", improvementName),
                ChatFormatting.DARK_GRAY + IModularItem.getImprovementDescription(key));

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(0, 8, improvementName);
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);
        
        if (current != preview) {
            value.setColor(preview ? GuiColors.add : GuiColors.remove);
        }

        label = new GuiStringOutline(0, -3, I18n.get("tetra.holo.craft.materials.stat_modifier"));
        label.setColor(GuiColors.muted);
        label.setAttachment(GuiAttachment.bottomCenter);
        addChild(label);
    }

    @Override
    public List<String> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
