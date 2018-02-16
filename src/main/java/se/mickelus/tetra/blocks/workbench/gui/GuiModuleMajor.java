package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ModuleData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GuiModuleMajor extends GuiElement {

    public GuiModuleMajor(int x, int y, ItemStack itemStack, ItemStack previewStack, String moduleClassName,
                          ItemModuleMajor module, ItemModuleMajor previewModule) {
        super(x, y, 0, 0);

        ModuleData data = module.getData(itemStack);
        ModuleData previewData = previewModule.getData(previewStack);

        if (data.equals(previewData)) {
            setupModule(module, data, itemStack, GuiModuleBackdrop.COLOR_NORMAL);
        } else {
            setupModule(previewModule, previewData, previewStack, GuiModuleBackdrop.COLOR_CHANGE);
        }

        addChild(new GuiStringSmall(19, 0, moduleClassName));
        setupImprovements(previewModule, previewStack, module, itemStack);
    }

    private void setupModule(ItemModuleMajor module, ModuleData data, ItemStack itemStack, int color) {
        addChild(new GuiModuleBackdrop(1, 0, color));
        addChild(new GuiString(19, 5, module.getName(itemStack), color));
        addChild(new GuiModuleGlyph(0, 0, 16, 16,
            data.glyph.tint, data.glyph.textureX, data.glyph.textureY,
            data.glyph.textureLocation));
    }

    private void setupImprovements(ItemModuleMajor previewModule, ItemStack previewStack, ItemModuleMajor module, ItemStack itemStack) {
        String[] improvements = getImprovementUnion(module.getImprovements(itemStack), previewModule.getImprovements(previewStack));
        for (int i = 0; i < improvements.length; i++) {
            int currentValue = module.getImprovementLevel(improvements[i], itemStack);
            int previewValue = previewModule.getImprovementLevel(improvements[i], previewStack);
            int color = GuiModuleBackdrop.COLOR_NORMAL;

            if (currentValue == 0) {
                color = GuiModuleBackdrop.COLOR_ADD;
            } else if (previewValue == 0) {
                color = GuiModuleBackdrop.COLOR_REMOVE;
            } else if (currentValue != previewValue) {
                color = GuiModuleBackdrop.COLOR_CHANGE;
            }

            addChild(new GuiModuleImprovement(19 + i * 4, 13, improvements[i], previewValue, color));
        }
    }

    public static String[] getImprovementUnion(String[] improvements, String[] previewImprovements) {
        Set<String> result = new HashSet<>(Arrays.asList(improvements));
        result.addAll(Arrays.asList(previewImprovements));
        return result.toArray(new String[result.size()]);
    }
}
