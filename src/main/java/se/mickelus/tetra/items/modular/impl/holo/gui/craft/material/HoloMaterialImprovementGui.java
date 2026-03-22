package se.mickelus.tetra.items.modular.impl.holo.gui.craft.material;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class HoloMaterialImprovementGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<Component> tooltip;
    List<Component> extendedTooltip;

    public HoloMaterialImprovementGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);

        String improvementName = I18n.exists("tetra.holo.craft.materials.stat_modifier." + key)
                ? I18n.get("tetra.holo.craft.materials.stat_modifier." + key)
                : IModularItem.getImprovementName(key, 0, null);

        if (I18n.exists("tetra.holo.craft.materials.stat_modifier." + key + ".tooltip")) {
            tooltip = Arrays.stream(I18n.get("tetra.holo.craft.materials.stat_modifier." + key + ".tooltip").split("\\n"))
                    .map(Component::literal)
                    .collect(ImmutableList.toImmutableList());
        } else {
            tooltip = ImmutableList.of(Component.translatable("tetra.holo.craft.materials.stat_modifier.tooltip", improvementName),
                    Component.literal(IModularItem.getImprovementDescription(key, 0, null)).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (I18n.exists("tetra.holo.craft.materials.stat_modifier." + key + ".tooltip_extended")) {
            extendedTooltip = Arrays.stream(I18n.get("tetra.holo.craft.materials.stat_modifier." + key + ".tooltip_extended").split("\\n"))
                    .map(Component::literal)
                    .collect(ImmutableList.toImmutableList());
        }

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
        if (hasFocus()) {
            if (Screen.hasShiftDown() && extendedTooltip != null) {
                return extendedTooltip;
            }
            return tooltip;
        }
        return null;
    }
}
