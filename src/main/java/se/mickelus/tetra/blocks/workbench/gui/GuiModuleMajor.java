package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ModuleData;

public class GuiModuleMajor extends GuiElement {

    public GuiModuleMajor(int x, int y, ItemStack itemStack, ItemStack previewStack, String moduleClassName,
                          ItemModuleMajor module, ItemModuleMajor previewModule) {
        super(x, y, 0, 0);

        ModuleData data = module.getData(itemStack);
        ModuleData previewData = previewModule.getData(previewStack);

        if (data.equals(previewData)) {
            setupChildren(module, data, itemStack, GuiModuleBackdrop.COLOR_NORMAL);
        } else {
            setupChildren(previewModule, previewData, previewStack, GuiModuleBackdrop.COLOR_CHANGE);
        }

        addChild(new GuiStringSmall(19, 0, moduleClassName));
    }

    private void setupChildren(ItemModule module, ModuleData data, ItemStack itemStack, int color) {
        addChild(new GuiModuleBackdrop(1, 0, color));
        addChild(new GuiString(19, 5, module.getName(itemStack), color));
        addChild(new GuiModuleGlyph(0, 0, 16, 16,
                data.glyph.tint, data.glyph.textureX, data.glyph.textureY,
                module.getGlyphLocation(itemStack)));
    }
}
