package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiClickable;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class HoloSlotGui extends GuiClickable {
    private final GuiTexture backdrop;
    private final GuiString slotString;

    public HoloSlotGui(int x, int y, GuiAttachment attachment, String slot, String label, Consumer<String> onSelect) {
        super(x, y, 11, 11, () -> onSelect.accept(slot));
        setAttachmentPoint(attachment);

        backdrop = new GuiTexture(-1, -1, 11, 11, 68, 0, GuiTextures.workbench);
        backdrop.setAttachmentPoint(attachment);
        backdrop.setAttachmentAnchor(attachment);
        addChild(backdrop);


        slotString = new GuiString(-15, 1, label);
        if (GuiAttachment.topLeft.equals(attachment)) {
            slotString.setX(14);
        }
        slotString.setAttachmentPoint(attachment);
        slotString.setAttachmentAnchor(attachment);
        addChild(slotString);

        width = slotString.getWidth() + 14;
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        backdrop.setColor(GuiColors.hover);
        slotString.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        backdrop.setColor(GuiColors.normal);
        slotString.setColor(GuiColors.normal);
    }
}
