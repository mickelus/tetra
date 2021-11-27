package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.function.Consumer;

public class HoloSchematicGui extends GuiElement {
    private final GuiElement listGroup;
    private final Consumer<OutcomePreview> onVariantOpen;
    private final HoloDescription description;
    private final HoloMaterialTranslation translation;
    private final HoloSortButton sortbutton;
    private final HoloFilterButton filterButton;
    private final HoloVariantListGui list;

    private HoloVariantDetailGui detail;

    private OutcomePreview openVariant;
    private OutcomePreview selectedVariant;
    private OutcomePreview hoveredVariant;

    String slot;

    private KeyframeAnimation showListAnimation;
    private KeyframeAnimation hideListAnimation;

    public HoloSchematicGui(int x, int y, int width, int height, Consumer<OutcomePreview> onVariantOpen) {
        super(x, y, width, height);

        this.onVariantOpen = onVariantOpen;

        listGroup = new GuiElement(0, 0, width, 64);
        addChild(listGroup);

        list = new HoloVariantListGui(0, 14, width, this::onVariantHover, this::onVariantBlur, this::onVariantSelect);
        listGroup.addChild(list);

        detail = new HoloVariantDetailGui(0, 68, width, onVariantOpen);
        addChild(detail);

        GuiHorizontalLayoutGroup buttons = new GuiHorizontalLayoutGroup(0, 0, 11, 6);
        listGroup.addChild(buttons);

        description = new HoloDescription(0, 0);
        buttons.addChild(description);

        translation = new HoloMaterialTranslation(0, 0);
        buttons.addChild(translation);

        sortbutton = new HoloSortButton(0, 0, this::onSortChange);
        buttons.addChild(sortbutton);

        filterButton = new HoloFilterButton(0, 0, this::onFilterChange);
        buttons.addChild(filterButton);

        showListAnimation = new KeyframeAnimation(60, listGroup)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(0))
        .withDelay(100);

        hideListAnimation = new KeyframeAnimation(100, listGroup)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(-50))
                .onStop(complete -> {
                    if (complete) {
                        list.setVisible(false);
                    }
                });;
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

    public void openVariant(OutcomePreview variant) {
        openVariant = variant;

        if (variant != null) {
            showListAnimation.stop();
            hideListAnimation.start();
            detail.showImprovements();
        } else {
            hideListAnimation.stop();
            showListAnimation.start();
            list.setVisible(true);
            detail.hideImprovements();
        }
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (selectedVariant != null && openVariant == null && keyCode == GLFW.GLFW_KEY_SPACE) {
            onVariantOpen.accept(selectedVariant);
            return true;
        }
        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    private void onVariantHover(OutcomePreview outcome) {
        if (!sortbutton.isBlockingFocus() && openVariant == null) {
            hoveredVariant = outcome;

            detail.updateVariant(selectedVariant, hoveredVariant, slot);
        }
    }

    private void onVariantBlur(OutcomePreview outcome) {
        if (outcome.equals(hoveredVariant) && openVariant == null) {
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
        detail.forceHide();

        listGroup.setY(0);
        listGroup.setOpacity(1);

        return true;
    }
}
