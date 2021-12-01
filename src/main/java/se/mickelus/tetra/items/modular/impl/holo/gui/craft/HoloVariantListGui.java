package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.mgui.gui.impl.GuiHorizontalScrollable;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;
import se.mickelus.tetra.gui.stats.sorting.StatSorters;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HoloVariantListGui extends GuiElement {
    private GuiHorizontalScrollable groupsScroll;
    private GuiHorizontalLayoutGroup groups;

    private Consumer<OutcomePreview> onVariantHover;
    private Consumer<OutcomePreview> onVariantBlur;
    private Consumer<OutcomePreview> onVariantSelect;

    private boolean filterCategory = false;
    private String filter = "";

    private IStatSorter sorter = StatSorters.none;

    private OutcomePreview[] previews;

    public HoloVariantListGui(int x, int y, int width, Consumer<OutcomePreview> onVariantHover, Consumer<OutcomePreview> onVariantBlur,
            Consumer<OutcomePreview> onVariantSelect) {
        super(x, y, width, 50);

        groupsScroll = new GuiHorizontalScrollable(0, 0, width, height).setGlobal(true);
        addChild(groupsScroll);
        groups = new GuiHorizontalLayoutGroup(0, 0, height, 12);
        groupsScroll.addChild(groups);

        this.onVariantHover = onVariantHover;
        this.onVariantBlur = onVariantBlur;
        this.onVariantSelect = onVariantSelect;
    }

    public void update(OutcomePreview[] previews) {
        this.previews = previews;
        this.filter = "";
        this.sorter = StatSorters.none;
        update();
    }

    private void update() {
        groups.clearChildren();

        Player player = Minecraft.getInstance().player;

        boolean isDevelopment = ConfigHandler.development.get();
        Map<String, List<OutcomePreview>> result = Arrays.stream(previews)
                .filter(preview -> isDevelopment || preview.materials.length != 0)
                .filter(this::filter)
                .collect(Collectors.groupingBy(preview -> preview.category, LinkedHashMap::new, Collectors.toList()));

        // categories start moving around if it's sorted before it's split up, so it's better to do it after
        if (sorter != StatSorters.none) {
            result.values().forEach(category -> category.sort(sorter.compare(player, preview -> preview.itemStack)));
        }

        // some wonk needed to do staggered animations of variants
        int offset = 0;
        for (Map.Entry<String, List<OutcomePreview>> entry : result.entrySet()) {
            groups.addChild(new HoloVariantGroupGui(0, 0, entry.getKey(), entry.getValue(), offset, sorter, player,
                    onVariantHover, onVariantBlur, onVariantSelect));
            offset += entry.getValue().size();
        }

        groupsScroll.markDirty();
    }

    public void updateSelection(OutcomePreview outcome) {
        groups.getChildren(HoloVariantGroupGui.class).forEach(group -> group.updateSelection(outcome));
    }

    @Override
    protected void onShow() {
        groups.getChildren(HoloVariantGroupGui.class).forEach(HoloVariantGroupGui::animateIn);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double distance) {
        if (isVisible()) {
            return super.onMouseScroll(mouseX, mouseY, distance);
        }

        return false;
    }

    @Override
    public boolean onCharType(char character, int modifiers) {
        if (character == 'f') {
            return true;
        }

        return false;
    }

    private boolean filter(OutcomePreview preview) {
        if (filter.length() == 0) {
            return true;
        }

        if (filterCategory) {
            return preview.category.contains(filter);
        }

        return preview.variantName.contains(filter);
    }

    public void updateFilter(String newValue) {
        filter = newValue.toLowerCase();

        filterCategory = filter.startsWith("#");
        if (filterCategory) {
            filter = filter.substring(1);
        }

        update();
    }

    public void changeSorting(IStatSorter sorter) {
        this.sorter = sorter;

        update();
    }
}
