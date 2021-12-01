package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HoloDescription extends GuiElement {
    private final List<String> emptyTooltip = Collections.singletonList(I18n.get("tetra.holo.craft.empty_description"));
    private List<String> tooltip;

    private GuiTexture icon;

    public HoloDescription(int x, int y) {
        super(x, y, 9, 9);

        icon = new GuiTexture(0, 0, 9, 9, 128, 32, GuiTextures.workbench);
        addChild(icon);
    }

    public void update(OutcomePreview[] previews) {
        tooltip = Arrays.stream(previews)
                .map(preview -> "tetra.module." + preview.moduleKey + ".description")
                .filter(I18n::exists)
                .map(I18n::get)
//                .map(description -> TextFormatting.GRAY + description)
//                .map(description -> description.replace("\n", "\n" + TextFormatting.GRAY))
//                .map(description -> description.replace(TextFormatting.RESET.toString(), TextFormatting.RESET.toString() + TextFormatting.GRAY))
                .map(Collections::singletonList)
                .findFirst()
                .orElse(emptyTooltip);
    }

    public void update(UpgradeSchematic schematic, ItemStack itemStack) {
        tooltip = ImmutableList.of(schematic.getDescription(itemStack));
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }
}
