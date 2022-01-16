package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class HoloDescription extends GuiElement {
    private final List<Component> emptyTooltip;
    private final GuiTexture icon;
    private List<Component> tooltip;

    public HoloDescription(int x, int y) {
        super(x, y, 9, 9);

        icon = new GuiTexture(0, 0, 9, 9, 128, 32, GuiTextures.workbench);
        addChild(icon);

        emptyTooltip = Collections.singletonList(new TranslatableComponent("tetra.holo.craft.empty_description"));
    }

    public void update(OutcomePreview[] previews) {
        tooltip = Arrays.stream(previews)
                .map(preview -> "tetra.module." + preview.moduleKey + ".description")
                .filter(I18n::exists)
                .map(TranslatableComponent::new)
//                .map(description -> TextFormatting.GRAY + description)
//                .map(description -> description.replace("\n", "\n" + TextFormatting.GRAY))
//                .map(description -> description.replace(TextFormatting.RESET.toString(), TextFormatting.RESET.toString() + TextFormatting.GRAY))
                .findFirst()
                .map(component -> (Component) component) // why is this necessary?
                .map(ImmutableList::of)
                .orElse(null);
    }

    public void update(UpgradeSchematic schematic, ItemStack itemStack) {
        tooltip = ImmutableList.of(new TextComponent(schematic.getDescription(itemStack)));
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }
}
