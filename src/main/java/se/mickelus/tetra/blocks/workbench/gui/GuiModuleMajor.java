package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GuiModuleMajor extends GuiModule {

    private GuiStringSmall slotString;

    private GuiModuleImprovement[] improvementElements;

    public GuiModuleMajor(int x, int y, GuiAttachment attachmentPoint, ItemStack itemStack, ItemStack previewStack,
                          String slotKey, String slotName,
                          ItemModuleMajor module, ItemModuleMajor previewModule,
                          Consumer<String> slotClickHandler, BiConsumer<String, String> hoverHandler) {
        super(x, y, attachmentPoint, itemStack, previewStack, slotKey, slotName, module, previewModule,
                slotClickHandler, hoverHandler);

        this.height = 17;

        improvementElements = new GuiModuleImprovement[0];
        if (module != null && previewModule != null) {
            setupImprovements(previewModule, previewStack, module, itemStack);
        }
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


            new KeyframeAnimation(100, slotString)
                    .withDelay(offset * 80 + 100)
                    .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(direction * 2, 0, true))
                    .start();

            for (int i = 0; i < improvementElements.length; i++) {
                new KeyframeAnimation(100, improvementElements[i])
                        .withDelay(offset * 200 + 280 + i * 80)
                        .applyTo(new Applier.Opacity(0, 1))
                        .start();
            }
        }
    }

    protected void setupChildren(String moduleName, GlyphData glyphData, String slotName, boolean tweakable) {
        backdrop = new GuiModuleBackdrop(1, 0, GuiColors.normal);
        backdrop.setAttachment(attachmentPoint);
        addChild(backdrop);

        if (tweakable) {
            tweakingIndicator = new GuiTextureOffset(1, 0, 15, 15, 96, 32, "textures/gui/workbench.png");
            tweakingIndicator.setAttachment(attachmentPoint);
            addChild(tweakingIndicator);
        }

        moduleString = new GuiString(19, 5, "");
        if (moduleName != null) {
            moduleString.setString(moduleName);
        } else {
            moduleString.setString(I18n.format("item.modular.empty_slot"));
        }
        if (GuiAttachment.topRight.equals(attachmentPoint)) {
            moduleString.setX(-16);
        }
        moduleString.setAttachment(attachmentPoint);
        addChild(moduleString);

        slotString = new GuiStringSmall(19, 0, slotName);
        if (GuiAttachment.topRight.equals(attachmentPoint)) {
            slotString.setX(-16);
        }
        slotString.setAttachment(attachmentPoint);
        addChild(slotString);

        width = moduleString.getWidth() + 19;

        if (glyphData != null) {
            glyph = new GuiModuleGlyph(0, 0, 16, 16,
                    glyphData.tint, glyphData.textureX, glyphData.textureY,
                    glyphData.textureLocation);
            if (GuiAttachment.topRight.equals(attachmentPoint)) {
                glyph.setX(1);
            }
            glyph.setAttachment(attachmentPoint);
            addChild(glyph);
        }
    }

    private void setupImprovements(ItemModuleMajor previewModule, ItemStack previewStack, ItemModuleMajor module,
            ItemStack itemStack) {
        String[] improvements = getImprovementUnion(module.getImprovements(itemStack), previewModule.getImprovements(previewStack));
        improvementElements = new GuiModuleImprovement[improvements.length];
        for (int i = 0; i < improvements.length; i++) {
            final String improvementKey = improvements[i];
            int currentValue = module.getImprovementLevel(itemStack, improvements[i]);
            int previewValue = previewModule.getImprovementLevel(previewStack, improvements[i]);
            int color;

            if (currentValue == -1) {
                color = GuiColors.add;
            } else if (previewValue == -1) {
                color = GuiColors.remove;
            } else if (currentValue != previewValue) {
                color = GuiColors.change;
            } else {
                color = module.getImprovement(itemStack, improvementKey).glyph.tint;
            }

            if (GuiAttachment.topRight.equals(attachmentPoint)) {
                improvementElements[i] = new GuiModuleImprovement(-17 + i * -5, 13, improvements[i], previewValue, color,
                        () -> hoverHandler.accept(slotKey, improvementKey),
                        () -> {
                            if (hasFocus()) {
                                hoverHandler.accept(slotKey, null);
                            }
                        });
            } else {
                improvementElements[i] = new GuiModuleImprovement(19 + i * 5, 13, improvements[i], previewValue, color,
                        () -> hoverHandler.accept(slotKey, improvementKey),
                        () -> {
                            if (hasFocus()) {
                                hoverHandler.accept(slotKey, null);
                            }
                        });
            }
            improvementElements[i].setAttachment(attachmentPoint);
            addChild(improvementElements[i]);
        }
    }

    public static String[] getImprovementUnion(ImprovementData[] improvements, ImprovementData[] previewImprovements) {
        return Stream.concat(Arrays.stream(improvements), Arrays.stream(previewImprovements))
                .map(improvement -> improvement.key)
                .distinct()
                .toArray(String[]::new);
    }

    protected void setColor(int color) {
        super.setColor(color);

        slotString.setColor(color);

        if(GuiColors.muted == color) {
            Arrays.stream(improvementElements).forEach(element -> element.setOpacity(0.5f));
        } else {
            Arrays.stream(improvementElements).forEach(element -> element.setOpacity(1));
        }
    }
}
