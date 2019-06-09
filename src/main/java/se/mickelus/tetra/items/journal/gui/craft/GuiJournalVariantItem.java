package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.schema.OutcomePreview;

import java.util.function.Consumer;

public class GuiJournalVariantItem extends GuiClickable {

    protected static final String texture = "textures/gui/workbench.png";
    protected GuiTexture backdrop;

    protected OutcomePreview outcome;
    protected Consumer<OutcomePreview> onHover;

    protected boolean isMuted = false;

    public GuiJournalVariantItem(int x, int y, int width, int height, OutcomePreview outcome,
                                 Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onSelect) {
        super(x, y, width, height, () -> onSelect.accept(outcome));

        this.outcome = outcome;
        this.onHover = onHover;
    }

    public GuiJournalVariantItem(int x, int y, OutcomePreview outcome,
                                 Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onSelect) {
        this(x, y, 11, 11, outcome, onHover, onSelect);

        backdrop = new GuiTexture(0, 0, 11, 11, 68, 0, texture);
        addChild(backdrop);

        addChild(new GuiModuleGlyph(2, 2, 8, 8, outcome.glyph).setShift(false));
    }

    public void updateSelection(OutcomePreview outcome) {
        isMuted = outcome != null && !this.outcome.equals(outcome);
        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        onHover.accept(outcome);

        backdrop.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        onHover.accept(null);

        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }
}
