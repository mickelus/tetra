package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
@ParametersAreNonnullByDefault
public class HoloVariantMajorItemGui extends HoloVariantItemGui {

    public HoloVariantMajorItemGui(int x, int y, OutcomePreview outcome, @Nullable String label,
            Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onBlur, Consumer<OutcomePreview> onSelect) {
        super(x, y, 16, 16, outcome, onHover, onBlur, onSelect);

        backdrop = new GuiTexture(1, 0, 15, 15, 52,0, GuiTextures.workbench);
        addChild(backdrop);

        if (label != null) {
            GuiString labelElement = new GuiStringOutline(label.startsWith("-") ? -2 : 1, 0, label);
            labelElement.setColor(outcome.glyph.tint);
            labelElement.setAttachment(GuiAttachment.middleCenter);
            addChild(labelElement);
        } else {
            addChild(new GuiModuleGlyph(0, 0, 16, 16, outcome.glyph).setShift(false));
        }

        material.setX(1);
        material.setY(0);
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        mouseX -= refX + x;
        mouseY -= refY + y;
        boolean gainFocus = true;

        if (mouseX + mouseY < 8) {
            gainFocus = false;
        }

        if (mouseX + mouseY > 24) {
            gainFocus = false;
        }

        if (mouseX - mouseY > 8) {
            gainFocus = false;
        }

        if (mouseY - mouseX > 8) {
            gainFocus = false;
        }

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }
}
