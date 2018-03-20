package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;


public class GuiModuleList extends GuiElement {

    private static int[][] minorYOffsets = { {12}, {5, 18}, {-1, 12, 25} };
    private static int[][] minorXOffsets = { {-21}, {-18, -18}, {-12, -21, -12} };

    private final Consumer<String> slotClickHandler;
    
    private GuiModuleMajor[] majorModuleElements;
    private GuiModuleMinor[] minorModuleElements;

    public GuiModuleList(int x, int y, Consumer<String> slotClickHandler) {
        super(x, y, 0, 0);

        majorModuleElements = new GuiModuleMajor[0];
        minorModuleElements = new GuiModuleMinor[0];

        this.slotClickHandler = slotClickHandler;
    }

    public void update(ItemStack itemStack, ItemStack previewStack, String focusSlot) {
        clearChildren();
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();

            updateMajorModules(item, itemStack, previewStack);
            updateMinorModules(item, itemStack, previewStack);

            setFocus(focusSlot);
        }
    }
    
    public void setFocus(String slotKey) {
        for (GuiModuleMajor element :
                majorModuleElements) {
            element.setFocusSlot(slotKey);
        }

        for (GuiModuleMinor element :
                minorModuleElements) {
            element.setFocusSlot(slotKey);
        }
    }

    private void updateMajorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] majorModuleNames = item.getMajorModuleNames();
        String[] majorModuleKeys = item.getMajorModuleKeys();
        ItemModuleMajor[] majorModules = item.getMajorModules(itemStack);
        
        majorModuleElements = new GuiModuleMajor[majorModules.length];

        if (!previewStack.isEmpty()) {
            ItemModuleMajor[] majorModulesPreview = item.getMajorModules(previewStack);
            for (int i = 0; i < majorModuleNames.length; i++) {
                majorModuleElements[i] = new GuiModuleMajor(4, i * 18, itemStack, previewStack,
                        majorModuleKeys[i], majorModuleNames[i],
                        majorModules[i], majorModulesPreview[i], slotClickHandler);
                addChild(majorModuleElements[i]);
            }
        } else {
            for (int i = 0; i < majorModuleNames.length; i++) {
                majorModuleElements[i] = new GuiModuleMajor(4, i * 18, itemStack, itemStack,
                        majorModuleKeys[i], majorModuleNames[i],
                        majorModules[i], majorModules[i], slotClickHandler);
                addChild(majorModuleElements[i]);
            }
        }
    }

    private void updateMinorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] minorModuleNames = item.getMinorModuleNames();
        String[] minorModuleKeys = item.getMinorModuleKeys();
        ItemModule[] minorModules = item.getMinorModules(itemStack);
        int index = 0;
        int count = minorModuleNames.length; //0;

        minorModuleElements = new GuiModuleMinor[minorModules.length];

        if (!previewStack.isEmpty()) {
            ItemModule[] minorModulesPreview = item.getMinorModules(previewStack);

//            for (int i = 0; i < minorModuleNames.length; i++) {
//                if (minorModules[i] != null || minorModulesPreview[i] != null) {
//                    count++;
//                }
//            }

            for (int i = 0; i < minorModuleNames.length; i++) {
//                if (minorModules[i] != null || minorModulesPreview[i] != null) {
                    minorModuleElements[i] = getMinorModule(count, index,
                        itemStack, previewStack, minorModuleKeys[i], minorModuleNames[i],
                        minorModules[i], minorModulesPreview[i]);
                addChild(minorModuleElements[i]);
                    index++;
//                }
            }
        } else {
//            count = (int) Arrays.stream(minorModules).filter(Objects::nonNull).count();


            for (int i = 0; i < minorModuleNames.length; i++) {
                minorModuleElements[i] = getMinorModule(count, index,
                    itemStack, itemStack, minorModuleKeys[i], minorModuleNames[i],
                    minorModules[i], minorModules[i]);
                addChild(minorModuleElements[i]);
                index++;
            }
        }
    }

    private GuiModuleMinor getMinorModule(int count, int index, ItemStack itemStack, ItemStack previewStack,
                                          String slotKey, String slotName,
                                          ItemModule module, ItemModule previewModule) {
        return new GuiModuleMinor(minorXOffsets[count-1][index], minorYOffsets[count-1][index], itemStack, previewStack,
            slotKey, slotName, module, previewModule, slotClickHandler);
    }
}
