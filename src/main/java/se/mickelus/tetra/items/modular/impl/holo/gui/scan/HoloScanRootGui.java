package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;

public class HoloScanRootGui extends HoloRootBaseGui {

    public HoloScanRootGui(int x, int y) {
        super(x, y);

        GuiString test = new GuiString(0, 0, TextFormatting.OBFUSCATED + "structures");
        test.setAttachment(GuiAttachment.middleCenter);
        addChild(test);
    }
}
