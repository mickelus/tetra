package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Arrays;
import java.util.Objects;


public class GuiModuleList extends GuiElement {

    private static int[][] minorYOffsets = { {13}, {6, 19}, {0, 13, 26} };
    private static int[][] minorXOffsets = { {-16}, {-4, -4}, {0, -8, 0} };

    public GuiModuleList(int x, int y) {
        super(x, y, 0, 0);
    }

    public void update(ItemStack itemStack, ItemStack previewStack) {
        if (itemStack.isEmpty()) {
            clearChildren();
            return;
        }

        ItemModular item = (ItemModular) itemStack.getItem();

        clearChildren();

        updateMajorModules(item, itemStack, previewStack);
        updateMinorModules(item, itemStack, previewStack);


    }

    private void updateMajorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] majorModuleNames = item.getMajorModuleNames();
        ItemModuleMajor[] majorModules = item.getMajorModules(itemStack);

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
    }

    private void updateMinorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] minorModuleNames = item.getMinorModuleNames();
        ItemModule[] minorModules = item.getMinorModules(itemStack);
        int index = 0;
        int count = 0;

        if (!previewStack.isEmpty()) {
            ItemModule[] minorModulesPreview = item.getMinorModules(previewStack);

            for (int i = 0; i < minorModuleNames.length; i++) {
                if (minorModules[i] != null || minorModulesPreview[i] != null) {
                    count++;
                }
            }

            for (int i = 0; i < minorModuleNames.length; i++) {
                if (minorModules[i] != null || minorModulesPreview[i] != null) {
                    addChild(getMinorModule(count, index,
                        itemStack, previewStack, minorModuleNames[i],
                        minorModules[i], minorModulesPreview[i]));
                    index++;
                }
            }
        } else {
            count = (int) Arrays.stream(minorModules).filter(Objects::nonNull).count();


            for (int i = 0; i < minorModuleNames.length; i++) {
                if (minorModules[i] != null) {
                    addChild(getMinorModule(count, index,
                        itemStack, itemStack, minorModuleNames[i],
                        minorModules[i], minorModules[i]));
                    index++;
                }
            }
        }
    }

    private GuiElement getMinorModule(int count, int index, ItemStack itemStack, ItemStack previewStack, String moduleClassName,
                                      ItemModule module, ItemModule previewModule) {
        minorYOffsets = new int[][] { {13}, {6, 20}, {0, 13, 26} };
        minorXOffsets = new int[][] { {-21}, {-18, -18}, {-12, -21, -12} };
        return new GuiModuleMinor(minorXOffsets[count-1][index], minorYOffsets[count-1][index], itemStack, previewStack,
            moduleClassName, module, previewModule);
    }
}
