package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ModuleData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GuiModule extends GuiClickable {

    protected String slotKey = null;
    protected GuiModuleBackdrop backdrop;
    protected GuiString moduleString;
    protected GuiModuleGlyph glyph;
    protected GuiTextureOffset tweakingIndicator;

    protected boolean isEmpty;
    protected boolean isHovered;
    protected boolean isUnselected;
    protected boolean isSelected;
    protected boolean isPreview;
    protected boolean isRemoving;
    protected boolean isAdding;

    protected BiConsumer<String, String> hoverHandler;

    public GuiModule(int x, int y, GuiAttachment attachmentPoint, ItemStack itemStack, ItemStack previewStack,
                     String slotKey, String slotName,
                     ItemModule module, ItemModule previewModule,
                     Consumer<String> slotClickHandler, BiConsumer<String, String> hoverHandler) {
        super(x, y, 0, 11, () -> slotClickHandler.accept(slotKey));
        this.slotKey = slotKey;
        setAttachmentPoint(attachmentPoint);

        if (module == null && previewModule == null) {
            isEmpty = true;
            setupChildren(null, null, slotName, false);
        } else if (previewModule == null) {
            isRemoving = true;
            ModuleData data = module.getData(itemStack);
            setupChildren(null, data.glyph, slotName, module.isTweakable(itemStack));
        } else if (module == null) {
            isAdding = true;
            ModuleData previewData = previewModule.getData(previewStack);
            setupChildren(previewModule.getName(previewStack), previewData.glyph, slotName, previewModule.isTweakable(itemStack));
        } else {
            ModuleData data = module.getData(itemStack);
            ModuleData previewData = previewModule.getData(previewStack);

            if (data.equals(previewData)) {
                setupChildren(module.getName(itemStack), data.glyph, slotName, module.isTweakable(itemStack));
            } else {
                isPreview = true;
                setupChildren(previewModule.getName(previewStack), previewData.glyph, slotName, previewModule.isTweakable(itemStack));
            }
        }

        this.hoverHandler = hoverHandler;
    }

    public void showAnimation(int offset) {
        if (isVisible()) {
            int direction = attachmentPoint == GuiAttachment.topLeft ? -2 : 2;
            new KeyframeAnimation(100, backdrop)
                    .withDelay(offset * 80)
                    .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(direction, 0, true))
                    .start();

            if (glyph != null) {
                new KeyframeAnimation(100, glyph)
                        .withDelay(offset * 80 + 100)
                        .applyTo(new Applier.Opacity(0, 1))
                        .start();
            }

            if (tweakingIndicator != null) {
                new KeyframeAnimation(100, tweakingIndicator)
                        .withDelay(offset * 80 + 100)
                        .applyTo(new Applier.Opacity(0, 1))
                        .start();
            }

            new KeyframeAnimation(100, moduleString)
                    .withDelay(offset * 80 + 200)
                    .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(direction * 2, 0, true))
                    .start();
        }
    }

    protected void setupChildren(String moduleName, GlyphData glyphData, String slotName, boolean tweakable) {
        backdrop = new GuiModuleMinorBackdrop(1, -1, GuiColors.normal);
        if (GuiAttachment.topLeft.equals(attachmentPoint)) {
            backdrop.setX(-1);
        }
        backdrop.setAttachment(attachmentPoint);
        addChild(backdrop);

        if (tweakable) {
            tweakingIndicator = new GuiTextureOffset(1, -1, 11, 11, 112, 32, "textures/gui/workbench.png");
            if (GuiAttachment.topLeft.equals(attachmentPoint)) {
                tweakingIndicator.setX(-1);
            }
            tweakingIndicator.setAttachment(attachmentPoint);
            addChild(tweakingIndicator);
        }


        moduleString = new GuiString(-12, 1, moduleName != null ? moduleName : slotName);
        if (GuiAttachment.topLeft.equals(attachmentPoint)) {
            moduleString.setX(12);
        }
        moduleString.setAttachment(attachmentPoint);
        addChild(moduleString);

        width = moduleString.getWidth() + 12;

        if (glyphData != null) {
            glyph = new GuiModuleGlyph(0, 1, 8, 8,
                    glyphData.tint, glyphData.textureX, glyphData.textureY,
                    glyphData.textureLocation);
            if (GuiAttachment.topLeft.equals(attachmentPoint)) {
                glyph.setX(1);
            }
            glyph.setAttachment(attachmentPoint);
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

    protected void setColor(int color) {
        backdrop.setColor(color);
        moduleString.setColor(color);
    }

    @Override
    protected void onFocus() {
        isHovered = true;
        updateColors();

        hoverHandler.accept(slotKey, null);
    }

    @Override
    protected void onBlur() {
        isHovered = false;
        updateColors();

        hoverHandler.accept(null, null);
    }
}
