package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.schema.OutcomePreview;

import java.util.function.Consumer;

public class GuiJournalVariantMajor extends GuiClickable {

    private final static String texture = "textures/gui/workbench.png";
    private final GuiTexture backdrop;

    private OutcomePreview outcome;
    private Consumer<OutcomePreview> onHover;

    public GuiJournalVariantMajor(int x, int y, OutcomePreview outcome,
            Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onSelect) {
        super(x, y, 16, 16, () -> onSelect.accept(outcome));

        this.outcome = outcome;
        this.onHover = onHover;

        backdrop = new GuiTexture(1, 0, 15, 15, 52,0, texture);
        addChild(backdrop);

        addChild(new GuiModuleGlyph(0, 0, 16, 16, outcome.glyph).setShift(false));
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
        backdrop.setColor(GuiColors.normal);
    }

}
