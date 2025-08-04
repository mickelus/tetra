package se.mickelus.tetra.items.modular.impl.holo.gui.craft.material;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class HoloMaterialFeatureGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    List<Component> tooltip;
    List<Component> extendedTooltip;

    public HoloMaterialFeatureGui(int x, int y, String key, boolean current, boolean preview) {
        super(x, y, 29, 29);


        if (I18n.exists("tetra.material.feature." + key + ".tooltip_extended")) {
            tooltip = ImmutableList.of(
                    Component.translatable("tetra.material.feature." + key + ".tooltip"),
                    Component.literal(" "),
                    Tooltips.expand);

            extendedTooltip = ImmutableList.of(
                    Component.translatable("tetra.material.feature." + key + ".tooltip"),
                    Component.literal(" "),
                    Tooltips.expand,
                    Component.translatable("tetra.material.feature." + key + ".tooltip_extended").withStyle(ChatFormatting.GRAY)
            );
        } else {
            tooltip = ImmutableList.of(
                    Component.translatable("tetra.material.feature." + key + ".tooltip"));
        }

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(0, 8, I18n.get("tetra.material.feature." + key));
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);

        if (current != preview) {
            value.setColor(preview ? GuiColors.add : GuiColors.remove);
        }

        label = new GuiStringOutline(0, -3, I18n.get("tetra.holo.craft.materials.feature"));
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
