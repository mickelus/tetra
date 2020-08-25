package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HoloVariantsGui extends GuiElement {
    private GuiHorizontalLayoutGroup groups;

    private Consumer<OutcomePreview> onVariantHover;
    private Consumer<OutcomePreview> onVariantBlur;
    private Consumer<OutcomePreview> onVariantSelect;

    public HoloVariantsGui(int x, int y, int width, Consumer<OutcomePreview> onVariantHover, Consumer<OutcomePreview> onVariantBlur,
            Consumer<OutcomePreview> onVariantSelect) {
        super(x, y, width, 50);

        groups = new GuiHorizontalLayoutGroup(0, 0, height, 12);
        addChild(groups);

        this.onVariantHover = onVariantHover;
        this.onVariantBlur = onVariantBlur;
        this.onVariantSelect = onVariantSelect;
    }

    public void update(ModularItem item, String slot, UpgradeSchematic schematic) {
        groups.clearChildren();

        Map<String, List<OutcomePreview>> result = Arrays.stream(schematic.getPreviews(new ItemStack(item), slot))
                .filter(preview -> preview.materials.length != 0)
                .collect(Collectors.groupingBy(preview -> preview.category, LinkedHashMap::new, Collectors.toList()));

        // some wonk needed to do staggered animations of variants
        int offset = 0;
        for (Map.Entry<String, List<OutcomePreview>> entry : result.entrySet()) {
            groups.addChild(new HoloVariantGroupGui(0, 0, entry.getKey(), entry.getValue(), offset,
                    onVariantHover, onVariantBlur, onVariantSelect));
            offset += entry.getValue().size();
        }
    }

    public void updateSelection(OutcomePreview outcome) {
        groups.getChildren(HoloVariantGroupGui.class).forEach(group -> group.updateSelection(outcome));
    }

    @Override
    protected void onShow() {
        groups.getChildren(HoloVariantGroupGui.class).forEach(HoloVariantGroupGui::animateIn);
    }
}
