package se.mickelus.tetra.items.journal.gui.system;

import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.tetra.items.journal.gui.GuiJournalRootBase;

public class GuiJournalSystemRoot extends GuiJournalRootBase {

    public GuiJournalSystemRoot(int x, int y) {
        super(x, y);
        GuiString test = new GuiString(0, 0, TextFormatting.OBFUSCATED + "system");
        test.setAttachment(GuiAttachment.middleCenter);
        addChild(test);
    }
}
