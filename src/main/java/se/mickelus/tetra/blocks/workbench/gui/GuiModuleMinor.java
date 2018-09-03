package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ModuleData;

import java.util.function.Consumer;

public class GuiModuleMinor extends GuiClickable {

    private final String slotKey;
    private GuiModuleMinorBackdrop backdrop;
    private GuiString moduleString;

    private boolean isEmpty;
    private boolean isHovered;
    private boolean isUnselected;
    private boolean isSelected;
    private boolean isPreview;
    private boolean isRemoving;
    private boolean isAdding;

    public GuiModuleMinor(int x, int y, GuiAttachment attachmentPoint, ItemStack itemStack, ItemStack previewStack, String slotKey, String slotName,
                          ItemModule module, ItemModule previewModule, Consumer<String> slotClickHandler) {
        super(x, y, 0, 11, () -> slotClickHandler.accept(slotKey));
        this.slotKey = slotKey;
        setAttachmentPoint(attachmentPoint);

        if (module == null && previewModule == null) {
            isEmpty = true;
            setupChildren(slotName, null);
        } else if (previewModule == null) {
            isRemoving = true;
            ModuleData data = module.getData(itemStack);
            setupChildren(slotName, data.glyph);
        } else if (module == null) {
            isAdding = true;
            ModuleData previewData = previewModule.getData(previewStack);
            setupChildren(previewModule.getName(previewStack), previewData.glyph);
        } else {
            ModuleData data = module.getData(itemStack);
            ModuleData previewData = previewModule.getData(previewStack);

            if (data.equals(previewData)) {
                setupChildren(module.getName(itemStack), data.glyph);
            } else {
                isPreview = true;
                setupChildren(previewModule.getName(previewStack), previewData.glyph);
            }
        }
        updateColors();
    }

    private void setupChildren(String moduleName, GlyphData glyphData) {
        backdrop = new GuiModuleMinorBackdrop(1, -1, GuiColors.normal);
        if (GuiAttachment.topLeft.equals(attachmentPoint)) {
            backdrop.setX(-1);
        }
        backdrop.setAttachmentPoint(attachmentPoint);
        backdrop.setAttachmentAnchor(attachmentPoint);
        addChild(backdrop);


        moduleString = new GuiString(-12, 1, moduleName);
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
    }

    public void updateSelectedHighlight(String selectedSlot) {
        isUnselected = selectedSlot != null && !slotKey.equals(selectedSlot);
        isSelected = selectedSlot != null && slotKey.equals(selectedSlot);
        updateColors();

    }

    private void updateColors() {
        if (isPreview) {
            setColor(GuiColors.change);
        } else if (isAdding) {
            setColor(GuiColors.add);
        } else if (isRemoving) {
            setColor(GuiColors.remove);
        } else if (isHovered && isEmpty) {
            setColor(GuiColors.hoverMuted);
        } else if (isHovered) {
            setColor(GuiColors.hover);
        } else if (isSelected) {
            setColor(GuiColors.normal);
        } else if (isEmpty || isUnselected) {
            setColor(GuiColors.muted);
        } else {
            setColor(GuiColors.normal);
        }
    }

    private void setColor(int color) {
        backdrop.setColor(color);
        moduleString.setColor(color);
    }

    @Override
    protected void onFocus() {
        isHovered = true;
        updateColors();
    }

    @Override
    protected void onBlur() {
        isHovered = false;
        updateColors();
    }
}
