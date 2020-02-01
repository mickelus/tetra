package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GuiModuleList extends GuiElement {
    private final Consumer<String> slotClickHandler;
    private final BiConsumer<String, String> hoverHandler;
    
    private GuiModuleMajor[] majorModuleElements;
    private GuiModule[] minorModuleElements;

    public GuiModuleList(int x, int y, Consumer<String> slotClickHandler, BiConsumer<String, String> hoverHandler) {
        super(x, y, 0, 0);

        majorModuleElements = new GuiModuleMajor[0];
        minorModuleElements = new GuiModule[0];

        this.slotClickHandler = slotClickHandler;
        this.hoverHandler = hoverHandler;
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

    public void showAnimation() {
        Random rand = new Random();
        for (int i = 0; i < majorModuleElements.length; i++) {
            majorModuleElements[i].showAnimation(rand.nextInt(minorModuleElements.length + majorModuleElements.length));
        }

        for (int i = 0; i < minorModuleElements.length; i++) {
            minorModuleElements[i].showAnimation(rand.nextInt(minorModuleElements.length + majorModuleElements.length));
        }
    }
    
    public void setFocus(String slotKey) {
        for (GuiModuleMajor element :
                majorModuleElements) {
            element.updateSelectedHighlight(slotKey);
        }

        for (GuiModule element :
                minorModuleElements) {
            element.updateSelectedHighlight(slotKey);
        }
    }

    private void updateMajorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] majorModuleNames = item.getMajorModuleNames();
        String[] majorModuleKeys = item.getMajorModuleKeys();
        ItemModuleMajor[] majorModules = item.getMajorModules(itemStack);
        GuiModuleOffsets offsets = GuiModuleOffsets.getMajorOffsets(item);
        
        majorModuleElements = new GuiModuleMajor[majorModules.length];

        if (!previewStack.isEmpty()) {
            ItemModuleMajor[] majorModulesPreview = item.getMajorModules(previewStack);
            for (int i = 0; i < majorModuleNames.length; i++) {
                final int x = offsets.getX(i);
                majorModuleElements[i] = new GuiModuleMajor(x, offsets.getY(i), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight,
                        itemStack, previewStack, majorModuleKeys[i], majorModuleNames[i],
                        majorModules[i], majorModulesPreview[i], slotClickHandler, hoverHandler);
                addChild(majorModuleElements[i]);
            }
        } else {
            for (int i = 0; i < majorModuleNames.length; i++) {
                final int x = offsets.getX(i);
                majorModuleElements[i] = new GuiModuleMajor(x, offsets.getY(i), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight,
                        itemStack, itemStack, majorModuleKeys[i], majorModuleNames[i],
                        majorModules[i], majorModules[i], slotClickHandler, hoverHandler);
                addChild(majorModuleElements[i]);
            }
        }
    }

    private void updateMinorModules(ItemModular item, ItemStack itemStack, ItemStack previewStack) {
        String[] minorModuleNames = item.getMinorModuleNames();
        String[] minorModuleKeys = item.getMinorModuleKeys();
        ItemModule[] minorModules = item.getMinorModules(itemStack);
        GuiModuleOffsets offsets = GuiModuleOffsets.getMinorOffsets(item);

        minorModuleElements = new GuiModule[minorModules.length];

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

    private GuiModule getMinorModule(int index, GuiModuleOffsets offsets, ItemStack itemStack, ItemStack previewStack,
                                     String slotKey, String slotName,
                                     ItemModule module, ItemModule previewModule) {
        final int x = offsets.getX(index);
        return new GuiModule(x, offsets.getY(index), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight,
                itemStack, previewStack, slotKey, slotName, module, previewModule, slotClickHandler, hoverHandler);
    }
}
