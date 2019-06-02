package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.schema.OutcomePreview;

import java.util.function.Consumer;

public class GuiJournalVariant extends GuiClickable {

    private final static String texture = "textures/gui/workbench.png";
    private final GuiTexture backdrop;

    private OutcomePreview outcome;
    private Consumer<OutcomePreview> onHover;

    public GuiJournalVariant(int x, int y, OutcomePreview outcome,
                             Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onSelect) {
        super(x, y, 11, 11, () -> onSelect.accept(outcome));

        this.outcome = outcome;
        this.onHover = onHover;

        backdrop = new GuiTexture(0, 0, 11, 11, 68, 0, texture);
        addChild(backdrop);

        addChild(new GuiModuleGlyph(2, 2, 8, 8, outcome.glyph).setShift(false));
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
