package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.schema.OutcomePreview;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiJournalSchema extends GuiElement {

    private GuiJournalVariants list;
    private GuiJournalVariantDetail detail;

    private OutcomePreview selectedVariant;
    private OutcomePreview hoveredVariant;

    String slot;

    public GuiJournalSchema(int x, int y, int width, int height) {
        super(x, y, width, height);

        list = new GuiJournalVariants(0, 0, width, this::onVariantHover, this::onVariantBlur, this::onVariantSelect);
        addChild(list);

        detail = new GuiJournalVariantDetail(0, 50, width);
        addChild(detail);

    }

    public void update(ItemModular item, String slot, UpgradeSchema schema) {
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
