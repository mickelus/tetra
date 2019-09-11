package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.gui.GuiAttachment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.module.ItemModule;

public class GuiTweakControls extends GuiElement {

    private GuiString untweakableLabel;

    public GuiTweakControls(int x, int y) {
        super(x, y, 224, 67);

        untweakableLabel = new GuiString(0, -3, TextFormatting.DARK_GRAY + I18n.format("workbench.module_detail.not_tweakable"));
        untweakableLabel.setAttachment(GuiAttachment.middleCenter);
        addChild(untweakableLabel);
    }

    public void update(ItemModule module, ItemStack itemStack) {
        if (module != null && module.isTweakable(itemStack)) {

            untweakableLabel.setVisible(false);
        } else {
            untweakableLabel.setVisible(true);
        }
    }
}
