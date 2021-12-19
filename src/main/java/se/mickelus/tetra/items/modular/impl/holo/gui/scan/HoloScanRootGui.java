package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import net.minecraft.ChatFormatting;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HoloScanRootGui extends HoloRootBaseGui {

    public HoloScanRootGui(int x, int y) {
        super(x, y);

        GuiString test = new GuiString(0, 0, ChatFormatting.OBFUSCATED + "structures");
        test.setAttachment(GuiAttachment.middleCenter);
        addChild(test);
    }
}
