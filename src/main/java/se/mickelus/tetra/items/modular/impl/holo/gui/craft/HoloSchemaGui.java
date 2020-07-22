package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.schema.OutcomePreview;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class HoloSchemaGui extends GuiElement {

    private HoloVariantsGui list;
    private HoloVariantDetailGui detail;

    private OutcomePreview selectedVariant;
    private OutcomePreview hoveredVariant;

    String slot;

    public HoloSchemaGui(int x, int y, int width, int height) {
        super(x, y, width, height);

        list = new HoloVariantsGui(0, 0, width, this::onVariantHover, this::onVariantBlur, this::onVariantSelect);
        addChild(list);

        detail = new HoloVariantDetailGui(0, 50, width);
        addChild(detail);

    }

    public void update(ModularItem item, String slot, UpgradeSchema schema) {
        list.update(item, slot, schema);

        this.slot = slot;

        selectedVariant = null;
        hoveredVariant = null;
        detail.updateVariant(null, null, slot);
    }

    private void onVariantHover(OutcomePreview outcome) {
        hoveredVariant = outcome;

        detail.updateVariant(selectedVariant, hoveredVariant, slot);
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
