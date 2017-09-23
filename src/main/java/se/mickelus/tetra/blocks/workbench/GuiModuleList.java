package se.mickelus.tetra.blocks.workbench;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiStringSmall;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;


public class GuiModuleList extends GuiElement {

    public GuiModuleList(int x, int y) {
        super(x, y, 0, 0);
    }

    public void update(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            clearChildren();
            return;
        }

        ItemModular item = (ItemModular) itemStack.getItem();
        String[] majorModuleNames = item.getMajorModuleNames();
        String[] minorModuleNames = item.getMinorModuleNames();

        ItemModuleMajor[] majorModules = item.getMajorModules(itemStack);
        ItemModule[] minorModules = item.getMinorModules(itemStack);

        clearChildren();

        for (int i = 0; i < majorModuleNames.length; i++) {
            addChild(new GuiModuleMajor(11, i * 18, itemStack, majorModules[i], majorModuleNames[i]));
        }

//        for (int i = 0; i < minorModuleNames.length; i++) {
//            addChild(new GuiStringSmall(0, ( i + minorModuleNames.length) * 12, minorModuleNames[i]));
//        }
    }
}
