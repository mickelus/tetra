package se.mickelus.tetra.items.journal.gui.blueprint;

import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.tetra.items.journal.gui.GuiJournalRootBase;

public class GuiJournalBlueprintRoot extends GuiJournalRootBase {

    public GuiJournalBlueprintRoot(int x, int y) {
        super(x, y);

        GuiString test = new GuiString(0, 0, TextFormatting.OBFUSCATED + "structures");
        test.setAttachment(GuiAttachment.middleCenter);
        addChild(test);
    }
}
