package se.mickelus.tetra.blocks.workbench;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ModuleData;

public class GuiModuleMajor extends GuiElement {

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
    }

    public GuiModuleMajor(int x, int y, ItemStack itemStack, ItemModuleMajor module, String moduleName) {
        super(x, y, 0, 0);

        ModuleData data = module.getData(itemStack);

        // icon backdrop
        addChild(new GuiTextureOffset(0, 0, 15, 15, "textures/gui/glyphs.png"));

        addChild(new GuiStringSmall(18, 0, moduleName));

        if (module != null) {
            addChild(new GuiString(18, 5, module.getName(itemStack)));
            addChild(new GuiModuleGlyph(0, 0, 15, 15, data.glyphTint, data.glyphTextureX, data.glyphTextureY, module.getGlyphLocation(itemStack)));
        } else {
            addChild(new GuiString(18, 5, "Empty"));
        }
    }
}
