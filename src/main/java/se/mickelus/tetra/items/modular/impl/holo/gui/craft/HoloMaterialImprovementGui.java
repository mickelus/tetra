package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class HoloMaterialImprovementGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<Component> tooltip;

    public HoloMaterialImprovementGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);

        String improvementName = IModularItem.getImprovementName(key, 0);

        tooltip = ImmutableList.of(new TranslatableComponent("tetra.holo.craft.materials.stat_modifier.tooltip", improvementName),
                new TextComponent(IModularItem.getImprovementDescription(key)).withStyle(ChatFormatting.DARK_GRAY));

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
    public List<Component> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
