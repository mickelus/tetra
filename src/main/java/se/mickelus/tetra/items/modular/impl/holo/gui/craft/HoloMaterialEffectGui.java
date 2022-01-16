package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class HoloMaterialEffectGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<Component> tooltip;

    public HoloMaterialEffectGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);

        tooltip = ImmutableList.of(
                new TranslatableComponent("tetra.holo.craft.materials.stat_effect.tooltip", I18n.get("tetra.stats." + key)),
                new TranslatableComponent("tetra.stats." + key + ".tooltip_short").withStyle(ChatFormatting.GRAY));

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(0, 8, I18n.get("tetra.stats." + key));
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);

        if (current != preview) {
            value.setColor(preview ? GuiColors.add : GuiColors.remove);
        }

        label = new GuiStringOutline(0, -3, I18n.get("tetra.holo.craft.materials.stat_effect"));
        label.setColor(GuiColors.muted);
        label.setAttachment(GuiAttachment.bottomCenter);
        addChild(label);
    }

    @Override
    public List<Component> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
