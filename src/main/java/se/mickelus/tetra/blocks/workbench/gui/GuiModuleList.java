package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;


public class GuiModuleList extends GuiElement {

    private static int[] minorYOffsets = { 12, 4, 0 };

    public GuiModuleList(int x, int y) {
        super(x, y, 0, 0);
    }

    public void update(ItemStack itemStack, ItemStack previewStack) {
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

        if (!previewStack.isEmpty()) {
            ItemModuleMajor[] majorModulesPreview = item.getMajorModules(previewStack);
            for (int i = 0; i < majorModuleNames.length; i++) {
                addChild(new GuiModuleMajor(11, i * 18, itemStack, previewStack, majorModuleNames[i], majorModules[i], majorModulesPreview[i]));
            }
        } else {
            for (int i = 0; i < majorModuleNames.length; i++) {
                addChild(new GuiModuleMajor(11, i * 18, itemStack, itemStack, majorModuleNames[i], majorModules[i], majorModules[i]));
            }
        }

        if (!previewStack.isEmpty()) {
            ItemModule[] minorModulesPreview = item.getMinorModules(previewStack);
            for (int i = 0; i < minorModuleNames.length; i++) {
                addChild(getMinorModule(i, minorModuleNames.length, itemStack, previewStack, minorModuleNames[i], minorModules[i], minorModulesPreview[i]));
            }
        } else {
            for (int i = 0; i < minorModuleNames.length; i++) {
                addChild(getMinorModule(i, minorModuleNames.length, itemStack, itemStack, minorModuleNames[i], minorModules[i], minorModules[i]));
            }
        }
    }

    private GuiElement getMinorModule(int index, int count, ItemStack itemStack, ItemStack previewStack, String moduleClassName,
                                      ItemModule module, ItemModule previewModule) {
        int offsetY = minorYOffsets[count - 1] + index * 12 + 1;
        int offsetX = (int) (0.6 * Math.abs(13 - offsetY) - 21);
        return new GuiModuleMinor(offsetX, offsetY, itemStack, previewStack, moduleClassName, module, previewModule);
    }
}
