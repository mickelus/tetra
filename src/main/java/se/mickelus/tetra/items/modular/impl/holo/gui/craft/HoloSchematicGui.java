package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

public class HoloSchematicGui extends GuiElement {

    private final HoloDescription description;
    private final HoloMaterialTranslation translation;
    private final HoloSortButton sortbutton;
    private final HoloFilterButton filterButton;
    private HoloVariantsGui list;
    private HoloVariantDetailGui detail;

    private OutcomePreview selectedVariant;
    private OutcomePreview hoveredVariant;

    String slot;

    public HoloSchematicGui(int x, int y, int width, int height) {
        super(x, y, width, height);

        list = new HoloVariantsGui(0, 14, width, this::onVariantHover, this::onVariantBlur, this::onVariantSelect);
        addChild(list);

        detail = new HoloVariantDetailGui(0, 64, width);
        addChild(detail);

        GuiHorizontalLayoutGroup buttons = new GuiHorizontalLayoutGroup(0, 0, 11, 6);
        addChild(buttons);

        description = new HoloDescription(0, 0);
        buttons.addChild(description);

        translation = new HoloMaterialTranslation(0, 0);
        buttons.addChild(translation);

        sortbutton = new HoloSortButton(0, 0, this::onSortChange);
        buttons.addChild(sortbutton);

        filterButton = new HoloFilterButton(0, 0, this::onFilterChange);
        buttons.addChild(filterButton);

    }

    public void update(IModularItem item, String slot, UpgradeSchematic schematic) {
        OutcomePreview[] previews = schematic.getPreviews(new ItemStack(item.getItem()), slot);
        list.update(previews);

        this.slot = slot;

        selectedVariant = null;
        hoveredVariant = null;
        detail.updateVariant(null, null, slot);

        filterButton.reset();
        sortbutton.update(previews);
        translation.update(schematic);
        description.update(previews);
    }

    private void onVariantHover(OutcomePreview outcome) {
        if (!sortbutton.isBlockingFocus()) {
            hoveredVariant = outcome;

            detail.updateVariant(selectedVariant, hoveredVariant, slot);
        }
    }

    private void onVariantBlur(OutcomePreview outcome) {
        if (outcome.equals(hoveredVariant)) {
            detail.updateVariant(selectedVariant, null, slot);
        }
    }

    private void onVariantSelect(OutcomePreview outcome) {
        selectedVariant = outcome;

        list.updateSelection(outcome);

        detail.updateVariant(selectedVariant, hoveredVariant, slot);
    }

    private void onFilterChange(String filter) {
        if (hoveredVariant != null) {
            detail.updateVariant(selectedVariant, null, slot);
        }

        list.updateFilter(filter);
    }

    private void onSortChange(IStatSorter sorter) {
        if (hoveredVariant != null) {
            detail.updateVariant(selectedVariant, null, slot);
        }

        list.changeSorting(sorter);
    }

    public void animateOpen() {
        list.onShow();
        if (selectedVariant != null) {
            detail.animateOpen();
        }
    }

    @Override
    protected void onShow() {
        list.setVisible(true);
    }

    @Override
    protected boolean onHide() {
        list.setVisible(false);
        detail.hide();
        return true;
    }
}
