package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.stats.IStatFormatter;
import net.minecraft.util.text.ITextComponent;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.ILabelGetter;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HoloMaterialStatGui extends GuiElement {
    private GuiTexture backdrop;

    private GuiString label;
    private GuiString value;

    private ILabelGetter valueFormatter;
    private Function<MaterialData, Float> getter;

    List<String> tooltip;

    public HoloMaterialStatGui(int x, int y, String key, ILabelGetter valueFormatter, Function<MaterialData, Float> getter) {
        super(x, y, 29, 29);

        this.valueFormatter = valueFormatter;
        this.getter = getter;

        tooltip = ImmutableList.of(I18n.format("tetra.holo.craft.materials.stat." + key + ".tooltip"));

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(1, 8, "");
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);

        label = new GuiStringOutline(0, -3, I18n.format("tetra.holo.craft.materials.stat." + key));
        label.setColor(GuiColors.muted);
        label.setAttachment(GuiAttachment.bottomCenter);
        addChild(label);
    }

    public void update(MaterialData current, MaterialData preview) {
        update(getter.apply(current), getter.apply(preview));
    }

    public void update(double current, double preview) {
        value.setString(valueFormatter.getLabelMerged(current, preview));
    }

    @Override
    public List<String> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
