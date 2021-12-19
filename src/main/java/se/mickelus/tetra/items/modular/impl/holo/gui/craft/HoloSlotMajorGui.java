package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiClickable;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class HoloSlotMajorGui extends GuiClickable {
    private final GuiTexture backdrop;
    private final GuiString slotString;

    public HoloSlotMajorGui(int x, int y, GuiAttachment attachment, String slot, String label, Consumer<String> onSelect) {
        super(x, y, 64, 17, () -> onSelect.accept(slot));
        setAttachmentPoint(attachment);

        backdrop = new GuiTexture(0, 0, 15, 15, 52, 0, GuiTextures.workbench);
        if (GuiAttachment.topRight.equals(attachment)) {
            backdrop.setX(-1);
        }
        backdrop.setAttachment(attachment);
        addChild(backdrop);

        slotString = new GuiString(18, 3, label);
        if (GuiAttachment.topRight.equals(attachment)) {
            slotString.setX(-18);
        }
        slotString.setAttachment(attachment);
        addChild(slotString);

        if ("".equals(label)) {
            slotString.setString(I18n.get("tetra.holo.craft.slot"));
        }

        width = slotString.getWidth() + 18;
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
