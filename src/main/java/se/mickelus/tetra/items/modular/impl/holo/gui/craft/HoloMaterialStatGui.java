package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.module.data.MaterialData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class HoloMaterialStatGui extends GuiElement {
    protected GuiTexture backdrop;

    protected GuiString label;
    protected GuiString value;

    protected ILabelGetter valueFormatter;
    protected Function<MaterialData, Float> getter;

    List<Component> tooltip;

    public HoloMaterialStatGui(int x, int y, String key, ILabelGetter valueFormatter, Function<MaterialData, Float> getter) {
        super(x, y, 29, 29);

        this.valueFormatter = valueFormatter;
        this.getter = getter;

        tooltip = ImmutableList.of(new TranslatableComponent("tetra.holo.craft.materials.stat." + key + ".tooltip"));

        backdrop = new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench);
        backdrop.setColor(0x222222);
        addChild(backdrop);

        value = new GuiStringOutline(1, 8, "");
        value.setAttachment(GuiAttachment.topCenter);
        addChild(value);

        label = new GuiStringOutline(0, -3, I18n.get("tetra.holo.craft.materials.stat." + key + ".short"));
        label.setColor(GuiColors.muted);
        label.setAttachment(GuiAttachment.bottomCenter);
        addChild(label);
    }

    public void update(MaterialData current, MaterialData preview) {
        update(getter.apply(current), getter.apply(preview));
    }

    protected void update(double current, double preview) {
        value.setColor(current == 0 && preview == 0 ? GuiColors.mutedStrong : GuiColors.normal);
        label.setColor(current == 0 && preview == 0 ? 0x222222 : GuiColors.muted);

        value.setString(valueFormatter.getLabelMerged(current, preview));
    }

    @Override
    public List<Component> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
