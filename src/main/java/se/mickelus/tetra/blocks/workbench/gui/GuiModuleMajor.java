package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.ImprovementData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class GuiModuleMajor extends GuiModule {

    private GuiStringSmall slotString;

    private GuiHorizontalLayoutGroup improvementGroup;

    public GuiModuleMajor(int x, int y, GuiAttachment attachmentPoint, ItemStack itemStack, ItemStack previewStack,
            String slotKey, String slotName,
            ItemModuleMajor module, ItemModuleMajor previewModule,
            Consumer<String> slotClickHandler, BiConsumer<String, String> hoverHandler) {
        super(x, y, attachmentPoint, itemStack, previewStack, slotKey, slotName, module, previewModule,
                slotClickHandler, hoverHandler);

        this.height = 17;

        improvementGroup = new GuiHorizontalLayoutGroup(GuiAttachment.topRight.equals(attachmentPoint) ? -17 : 19, 13, 3, 1);
        improvementGroup.setAttachment(attachmentPoint);
        addChild(improvementGroup);

        if (module != null && previewModule != null) {
            setupImprovements(previewModule, previewStack, module, itemStack);
        }
    }

    public static String[] getImprovementUnion(ImprovementData[] improvements, ImprovementData[] previewImprovements) {
        return Stream.concat(Arrays.stream(improvements), Arrays.stream(previewImprovements))
                .map(improvement -> improvement.key)
                .distinct()
                .toArray(String[]::new);
    }

    public static List<Enchantment> getEnchantmentUnion(Set<Enchantment> enchantments, Set<Enchantment> previewEnchantments) {
        return Stream.concat(enchantments.stream(), previewEnchantments.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    protected void setupChildren(String moduleName, GlyphData glyphData, String slotName, boolean tweakable) {
        backdrop = new GuiModuleBackdrop(1, 0, GuiColors.normal);
        backdrop.setAttachment(attachmentPoint);
        addChild(backdrop);

        if (tweakable) {
            tweakingIndicator = new GuiTextureOffset(1, 0, 15, 15, 96, 32, GuiTextures.workbench);
            tweakingIndicator.setAttachment(attachmentPoint);
            addChild(tweakingIndicator);
        }

        moduleString = new GuiString(19, 5, "");
        if (moduleName != null) {
            moduleString.setString(moduleName);
        } else {
            moduleString.setString(I18n.get("item.tetra.modular.empty_slot"));
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

            for (int i = 0; i < improvementGroup.getNumChildren(); i++) {
                GuiElement element = improvementGroup.getChild(i);
                element.setOpacity(0);
                new KeyframeAnimation(100, element)
                        .withDelay(offset * 200 + 280 + i * 80)
                        .applyTo(new Applier.Opacity(1))
                        .start();
            }
        }
    }

    private void setupImprovements(ItemModuleMajor previewModule, ItemStack previewStack, ItemModuleMajor module, ItemStack itemStack) {
        improvementGroup.clearChildren();

        String[] improvements = getImprovementUnion(module.getImprovements(itemStack), previewModule.getImprovements(previewStack));

        for (String improvementKey : improvements) {
            int currentValue = module.getImprovementLevel(itemStack, improvementKey);
            int previewValue = previewModule.getImprovementLevel(previewStack, improvementKey);
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

            GuiModuleImprovement improvement = new GuiModuleImprovement(0, 0, improvementKey, previewValue, color,
                    () -> hoverHandler.accept(slotKey, improvementKey),
                    () -> {
                        if (hasFocus()) {
                            hoverHandler.accept(slotKey, null);
                        }
                    });

            improvementGroup.addChild(improvement);
        }

        Map<Enchantment, Integer> currentEnchantments = module.getEnchantments(itemStack);
        Map<Enchantment, Integer> previewEnchantments = module.getEnchantments(previewStack);
        getEnchantmentUnion(currentEnchantments.keySet(), previewEnchantments.keySet()).forEach(enchantment -> {
            int color;

            int currentLevel = currentEnchantments.getOrDefault(enchantment, 0);
            int previewLevel = previewEnchantments.getOrDefault(enchantment, 0);

            if (currentLevel == 0) {
                color = GuiColors.add;
            } else if (previewLevel == 0) {
                color = GuiColors.remove;
            } else if (currentLevel != previewLevel) {
                color = GuiColors.change;
            } else {
                color = GuiColors.normal;
            }

            String enchantmentKey = "enchantment:" + Registry.ENCHANTMENT.getKey(enchantment).toString();
            improvementGroup.addChild(new GuiModuleEnchantment(0, 0, enchantment, previewLevel, color,
                    () -> hoverHandler.accept(slotKey, enchantmentKey),
                    () -> {
                        if (hasFocus()) {
                            hoverHandler.accept(slotKey, null);
                        }
                    }));
        });

        improvementGroup.forceLayout();
    }

    protected void setColor(int color) {
        super.setColor(color);

        slotString.setColor(color);

        improvementGroup.setOpacity(color == GuiColors.muted ? 0.5f : 1);
    }
}
