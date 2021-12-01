package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.world.item.ItemStack;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
@ParametersAreNonnullByDefault
public class HoloImprovementGui extends GuiElement {
    private final GuiHorizontalLayoutGroup header;
    private final GuiTexture backdrop;
    private final GuiTexture plus;
    private final GuiString label;

    private final HoloDescription description;
    private final HoloMaterialTranslation translation;

    private final GuiElement variants;

    private UpgradeSchematic schematic;
    private String slot;

    private boolean isActive;

    private OutcomePreview preview;
    private Consumer<OutcomePreview> onVariantHover;
    private Consumer<OutcomePreview> onVariantBlur;

    private final Consumer<OutcomeStack> onVariantSelect;

    public HoloImprovementGui(int x, int y, UpgradeSchematic schematic, ItemStack baseStack, String slot, Consumer<OutcomePreview> onVariantHover,
            Consumer<OutcomePreview> onVariantBlur, Consumer<OutcomeStack> onVariantSelect) {
        super(x, y, 52, 16);

        this.onVariantHover = onVariantHover;
        this.onVariantBlur = onVariantBlur;

        this.onVariantSelect = onVariantSelect;

        OutcomePreview[] previews = schematic.getPreviews(baseStack, slot);

        backdrop = new GuiTexture(1, 5, 16, 9, 52, 3, GuiTextures.workbench);
        addChild(backdrop);

        addChild(new GuiModuleGlyph(0, 2, 16, 16, schematic.getGlyph()).setShift(false));

        plus = new GuiTexture(7, 10, 7, 7, 68, 16, GuiTextures.workbench);
        plus.setColor(GuiColors.muted);
        addChild(plus);

        header = new GuiHorizontalLayoutGroup(24, 0, 0, 4);
        addChild(header);

        label = new GuiString(0, 0, schematic.getName());
        header.addChild(label);

        description = new HoloDescription(0, 0);
        description.update(schematic, baseStack);
        header.addChild(description);

        translation = new HoloMaterialTranslation(0, 0);
        translation.update(schematic);
        header.addChild(translation);

        variants = new GuiElement(24, 11, 0, 11);
        addChild(variants);

        this.schematic = schematic;
        this.slot = slot;

        header.forceLayout();


        updateVariants(previews, Collections.emptyList());
    }

    public void updateVariants(OutcomePreview[] previews, List<OutcomeStack> selectedOutcomes) {
        variants.clearChildren();

        List<OutcomePreview> matchingSelections = selectedOutcomes.stream()
                .filter(stack -> stack.schematicEquals(schematic))
                .map(stack -> stack.preview)
                .collect(Collectors.toList());
        isActive = !matchingSelections.isEmpty();

        this.preview = null;

        if (previews.length > 1) {
            int labelStart = findLabelStart(previews);
            for (int i = 0; i < previews.length; i++) {
                String labelString = previews[i].variantName;

                boolean isConnected = i + 1 < previews.length && Objects.equals(previews[i].variantKey, previews[i + 1].variantKey);

                HoloImprovementVariantGui variant = new HoloImprovementVariantGui(i * 28, 0, labelString, labelStart, previews[i], isConnected,
                        onVariantHover, onVariantBlur,
                        preview -> onVariantSelect.accept(new OutcomeStack(schematic, preview)));
                variants.addChild(variant);

                if (!matchingSelections.isEmpty()) {
                    variant.setMuted(!matchingSelections.contains(previews[i]));
                }

                backdrop.setColor(GuiColors.normal);
                label.setColor(GuiColors.normal);

                variants.setVisible(true);
            }

            header.setY(0);

            setWidth(Math.max(variants.getX() + previews.length * 28 - 9, header.getWidth()));

            updateTint(false);
        } else {
            header.setY(6);
            variants.setVisible(false);

            if (previews.length == 1) {
                this.preview = previews[0];
            }

            setWidth(header.getWidth() + 24);

            updateTint(hasFocus());
        }

    }

    public void updateSelection(ItemStack itemStack, List<OutcomeStack> selectedOutcomes) {
        OutcomePreview[] previews = schematic.getPreviews(itemStack, slot);
        updateVariants(previews, selectedOutcomes);
    }

    private int findLabelStart(OutcomePreview[] previews) {
        int maxLength = Arrays.stream(previews)
                .map(preview -> preview.variantName)
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .max()
                .orElse(0);

        Character cmp = null;
        for (int j = 0; j < maxLength; j++) {
            for (int i = 0; i < previews.length; i++) {
                String name = previews[i].variantName;
                if (name != null && name.length() > j) {
                    if (cmp == null) {
                        cmp = name.charAt(j);
                    } else if (!cmp.equals(name.charAt(j))) {
                        return j;
                    }
                }
            }

            cmp = null;
        }

        return 0;
    }

    protected void updateTint(boolean hasFocus) {
        if (isActive) {
            backdrop.setColor(GuiColors.hoverMuted);
            label.setColor(GuiColors.hover);
            plus.setColor(GuiColors.hover);

        } else {
            backdrop.setColor(GuiColors.normal);
            label.setColor(GuiColors.normal);
            plus.setColor(GuiColors.muted);

        }

        if (hasFocus) {
            backdrop.setColor(GuiColors.hover);
            label.setColor(GuiColors.hover);
        }
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        if (!variants.isVisible()) {
            updateTint(true);

            if (preview != null) {
                onVariantHover.accept(preview);
            }
        }
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        if (!variants.isVisible()) {
            updateTint(false);

            if (preview != null) {
                onVariantBlur.accept(preview);
            }
        }
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (hasFocus() && preview != null) {
            boolean wasActive = isActive;
            onVariantSelect.accept(new OutcomeStack(schematic, preview));

            if (wasActive) {
                onVariantHover.accept(preview);
            }

            return true;
        }

        return super.onMouseClick(x, y, button);
    }
}
