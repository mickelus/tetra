package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.GlyphData;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ModuleData;

public class GuiModuleMinor extends GuiElement {

    public GuiModuleMinor(int x, int y, ItemStack itemStack, ItemStack previewStack, String moduleClassName,
                          ItemModule module, ItemModule previewModule) {
        super(x, y, 0, 0);

        if (module == null && previewModule == null) {
            setupChildren(moduleClassName, null, 0xaaaaaa);
        } else if (previewModule == null) {
            setupChildren(moduleClassName, null, GuiModuleBackdrop.COLOR_REMOVE);
        } else if (module == null) {
            setupChildren(moduleClassName, null, GuiModuleBackdrop.COLOR_ADD);
        } else {
            ModuleData data = module.getData(itemStack);
            ModuleData previewData = previewModule.getData(previewStack);

            if (data.equals(previewData)) {
                setupChildren(module.getName(itemStack), data.glyph, GuiModuleBackdrop.COLOR_NORMAL);
            } else {
                setupChildren(previewModule.getName(itemStack), previewData.glyph, GuiModuleBackdrop.COLOR_CHANGE);
            }
        }

    }

    private void setupChildren(String moduleName, GlyphData glyphData, int color) {
        addChild(new GuiModuleMinorBackdrop(-10, -2, color));
        addChild(new GuiString(-10, 0, moduleName, color, GuiAlignment.right));

        if (glyphData != null) {
            addChild(new GuiModuleGlyph(-8, 0, 8, 8,
                glyphData.tint, glyphData.textureX, glyphData.textureY,
                glyphData.textureLocation));
        }
    }
}
