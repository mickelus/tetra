package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.ILabelGetter;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.List;
import java.util.function.Function;

public class HoloMaterialEffectGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<String> tooltip;

    public HoloMaterialEffectGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);

        tooltip = ImmutableList.of(
                I18n.format("tetra.holo.craft.materials.stat_effect.tooltip", I18n.format("tetra.stats." + key)),
                TextFormatting.GRAY + I18n.format("tetra.stats." + key + ".tooltip_short"));

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(0, 8, I18n.format("tetra.stats." + key));
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);

        if (current != preview) {
            value.setColor(preview ? GuiColors.add : GuiColors.remove);
        }

        label = new GuiStringOutline(0, -3, I18n.format("tetra.holo.craft.materials.stat_effect"));
        label.setColor(GuiColors.muted);
        label.setAttachment(GuiAttachment.bottomCenter);
        addChild(label);
    }

    @Override
    public List<String> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
