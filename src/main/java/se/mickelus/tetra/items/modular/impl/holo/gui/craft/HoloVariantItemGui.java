package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mgui.gui.GuiClickable;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import java.util.function.Consumer;

public class HoloVariantItemGui extends GuiClickable {
    protected GuiTexture backdrop;

    protected OutcomePreview outcome;
    protected Consumer<OutcomePreview> onHover;
    protected Consumer<OutcomePreview> onBlur;

    protected boolean isMuted = false;

    public HoloVariantItemGui(int x, int y, int width, int height, OutcomePreview outcome,
            Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onBlur, Consumer<OutcomePreview> onSelect) {
        super(x, y, width, height, () -> onSelect.accept(outcome));

        this.outcome = outcome;
        this.onHover = onHover;
        this.onBlur = onBlur;
    }

    public HoloVariantItemGui(int x, int y, OutcomePreview outcome,
            Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onBlur, Consumer<OutcomePreview> onSelect) {
        this(x, y, 11, 11, outcome, onHover, onBlur, onSelect);

        backdrop = new GuiTexture(0, 0, 11, 11, 68, 0, GuiTextures.workbench);
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

        onBlur.accept(outcome);

        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }
}
