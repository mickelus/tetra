package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ModuleData;

import java.util.function.Consumer;

public class GuiModuleMinor extends GuiClickable {

    private final String slotKey;
    private int color;
    private GuiModuleMinorBackdrop backdrop;
    private GuiString moduleString;

    private boolean isEmpty;

    public GuiModuleMinor(int x, int y, GuiAttachment attachmentPoint, ItemStack itemStack, ItemStack previewStack, String slotKey, String slotName,
                          ItemModule module, ItemModule previewModule, Consumer<String> slotClickHandler) {
        super(x, y, 0, 11, () -> slotClickHandler.accept(slotKey));
        this.slotKey = slotKey;
        setAttachmentPoint(attachmentPoint);

        if (module == null && previewModule == null) {
            isEmpty = true;
            setupChildren(slotName, null, GuiColors.muted);
        } else if (previewModule == null) {
            ModuleData data = module.getData(itemStack);
            setupChildren(slotName, data.glyph, GuiColors.remove);
        } else if (module == null) {
            ModuleData previewData = previewModule.getData(previewStack);
            setupChildren(slotName, previewData.glyph, GuiColors.add);
        } else {
            ModuleData data = module.getData(itemStack);
            ModuleData previewData = previewModule.getData(previewStack);

            if (data.equals(previewData)) {
                setupChildren(module.getName(itemStack), data.glyph, GuiColors.normal);
            } else {
                setupChildren(previewModule.getName(previewStack), previewData.glyph, GuiColors.change);
            }
        }
    }

    private void setupChildren(String moduleName, GlyphData glyphData, int color) {
        backdrop = new GuiModuleMinorBackdrop(1, -1, color);
        if (GuiAttachment.topLeft.equals(attachmentPoint)) {
            backdrop.setX(-1);
        }
        backdrop.setAttachmentPoint(attachmentPoint);
        backdrop.setAttachmentAnchor(attachmentPoint);
        addChild(backdrop);


        moduleString = new GuiString(-12, 1, moduleName, color);
        if (GuiAttachment.topLeft.equals(attachmentPoint)) {
            moduleString.setX(12);
        }
        moduleString.setAttachmentPoint(attachmentPoint);
        moduleString.setAttachmentAnchor(attachmentPoint);
        addChild(moduleString);

        width = moduleString.getWidth() + 12;

        if (glyphData != null) {
            final GuiModuleGlyph glyph = new GuiModuleGlyph(0, 1, 8, 8,
                    glyphData.tint, glyphData.textureX, glyphData.textureY,
                    glyphData.textureLocation);
            if (GuiAttachment.topLeft.equals(attachmentPoint)) {
                glyph.setX(1);
            }
            glyph.setAttachmentPoint(attachmentPoint);
            glyph.setAttachmentAnchor(attachmentPoint);
            addChild(glyph);
        }


        this.color = color;
    }

    public void setFocusSlot(String focusSlotKey) {
        if (slotKey.equals(focusSlotKey)) {
            color = GuiColors.normal;
        } else if (!isEmpty && focusSlotKey == null) {
            color = GuiColors.normal;
        } else {
            color = GuiColors.muted;
        }
        setColors(color);
    }

    private void setColors(int color) {
        backdrop.setColor(color);
        moduleString.setColor(color);
    }

    @Override
    protected void onFocus() {
        setColors(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        setColors(color);
    }
}
