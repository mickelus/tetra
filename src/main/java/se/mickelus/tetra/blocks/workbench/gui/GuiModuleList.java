package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiAttachment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.function.Consumer;

public class GuiModuleList extends GuiElement {
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
        Offsets offsets = Offsets.getMajorOffsets(item);
        
        majorModuleElements = new GuiModuleMajor[majorModules.length];

        if (!previewStack.isEmpty()) {
            ItemModuleMajor[] majorModulesPreview = item.getMajorModules(previewStack);
            for (int i = 0; i < majorModuleNames.length; i++) {
                final int x = offsets.getX(i);
                majorModuleElements[i] = new GuiModuleMajor(x, offsets.getY(i), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight,
                        itemStack, previewStack, majorModuleKeys[i], majorModuleNames[i],
                        majorModules[i], majorModulesPreview[i], slotClickHandler);
                addChild(majorModuleElements[i]);
            }
        } else {
            for (int i = 0; i < majorModuleNames.length; i++) {
                final int x = offsets.getX(i);
                majorModuleElements[i] = new GuiModuleMajor(x, offsets.getY(i), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight,
                        itemStack, itemStack, majorModuleKeys[i], majorModuleNames[i],
                        majorModules[i], majorModules[i], slotClickHandler);
                addChild(majorModuleElements[i]);
            }
        }
    }

    private void updateMinorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] minorModuleNames = item.getMinorModuleNames();
        String[] minorModuleKeys = item.getMinorModuleKeys();
        ItemModule[] minorModules = item.getMinorModules(itemStack);
        Offsets offsets = Offsets.getMinorOffsets(item);

        minorModuleElements = new GuiModuleMinor[minorModules.length];

        if (!previewStack.isEmpty()) {
            ItemModule[] minorModulesPreview = item.getMinorModules(previewStack);

            for (int i = 0; i < minorModuleNames.length; i++) {
                minorModuleElements[i] = getMinorModule(i, offsets, itemStack, previewStack,
                        minorModuleKeys[i], minorModuleNames[i], minorModules[i], minorModulesPreview[i]);
                addChild(minorModuleElements[i]);
            }
        } else {
            for (int i = 0; i < minorModuleNames.length; i++) {
                minorModuleElements[i] = getMinorModule(i, offsets,
                    itemStack, itemStack, minorModuleKeys[i], minorModuleNames[i],
                    minorModules[i], minorModules[i]);
                addChild(minorModuleElements[i]);
            }
        }
    }

    private GuiModuleMinor getMinorModule(int index, Offsets offsets, ItemStack itemStack, ItemStack previewStack,
                                          String slotKey, String slotName,
                                          ItemModule module, ItemModule previewModule) {
        final int x = offsets.getX(index);
        return new GuiModuleMinor(x, offsets.getY(index), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight,
                itemStack, previewStack, slotKey, slotName, module, previewModule, slotClickHandler);
    }
    
    private static class Offsets {
        static Offsets[] defaultMajorOffsets = {
                new Offsets(4, 0),
                new Offsets(4, 0, 4, 18),
                new Offsets(4, 0, 4, 18, -4, 0),
                new Offsets(4, 0, 4, 18, -4, 0, -4, 18)
        };

        static Offsets[] defaultMinorOffsets = {
                new Offsets(-21, 12),
                new Offsets(-18, 5, -18, 18),
                new Offsets(-12, -1, -21, 12, -12, 25),
        };

        static Offsets toolbeltMajorOffsets = new Offsets();
        static Offsets toolbeltMinorOffsets = new Offsets();

        private int[] offsetX;
        private int[] offsetY;
        private boolean[] alignment;

        public Offsets(int ... offsets) {
            offsetX = new int[offsets.length / 2];
            offsetY = new int[offsets.length / 2];
            alignment = new boolean[offsets.length / 2];
            for (int i = 0; i < offsets.length / 2; i++) {
                offsetX[i] = offsets[i * 2];
                offsetY[i] = offsets[i * 2 + 1];
                alignment[i] = offsetX[i] > 0;
            }
        }

        public int size() {
            return offsetX.length;
        }

        public int getX(int index) {
            return offsetX[index];
        }

        public int getY(int index) {
            return offsetY[index];
        }

        public boolean getAlignment(int index) {
            return alignment[index];
        }

        public static Offsets getMajorOffsets(ItemModular item) {
            if (item instanceof ItemToolbeltModular) {
                return new Offsets(-14, 18, 4, 0, 4, 18);
            } else {
                return defaultMajorOffsets[item.getNumMajorModules() - 1];
            }
        }

        public static Offsets getMinorOffsets(ItemModular item) {
            if (item instanceof ItemToolbeltModular) {
                return new Offsets(-13, 0);
            } else {
                return defaultMinorOffsets[item.getNumMinorModules() - 1];
            }
        }
    }
}
