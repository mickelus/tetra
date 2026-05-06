package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiStringSmall;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuiSources extends GuiElement {
    private List<Component> tooltip = Collections.singletonList(Component.translatable("tetra.sources.unknown_tooltip"));
    private GuiTexture icon;
    private final GuiStringSmall label;

    public GuiSources(int x, int y, int width) {
        super(x, y, width, 5);

        icon = new GuiTexture(0, 0, 4, 4, 214, 33, GuiTextures.workbench);
//        addChild(icon);
        label = new GuiStringSmall(0, 0, "");
        label.setColor(GuiColors.source);
        addChild(label);
    }

    public void update(UpgradeSchematic schematic) {
        String[] sources = schematic.getSources();

        label.setColor(0x5555FF);
        if (sources.length > 0) {
            if (sources.length > 1) {
                String modifiers = String.join(", ", Arrays.copyOfRange(sources, 0, sources.length - 1));
                tooltip = Collections.singletonList(Component.literal(I18n.get("tetra.sources.multi_tooltip", sources[sources.length - 1], modifiers)));
            } else {
                tooltip = Collections.singletonList(Component.literal(I18n.get("tetra.sources.single_tooltip", sources[0])));
            }

            Font font = Minecraft.getInstance().font;
            for (int i = sources.length; i > 0; i--) {
                String labelString = String.join(", ", Arrays.copyOfRange(sources, 0, i));
                String overflowSuffix = I18n.get("tetra.sources.overflow_suffix_label", sources.length - i);
                int width = font.width(labelString);
                if (i == sources.length && width < this.width - 7) {
                    label.setString(labelString);
                    return;
                } else if (width < this.width - 7 - font.width(overflowSuffix)) {
                    label.setString(labelString + overflowSuffix);
                    return;
                }
            }
            label.setString(I18n.get("tetra.sources.overflow_label", sources.length));
        } else {
            label.setString(I18n.get("tetra.sources.unknown_label"));
            tooltip = Collections.singletonList(Component.translatable("tetra.sources.unknown_tooltip"));
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }
}
