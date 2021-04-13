package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class HoloVariantGroupGui extends GuiElement {

    private final GuiElement variantsContainer;

    private final KeyframeAnimation labelAnimation;
    private final KeyframeAnimation[] itemAnimations;

    public HoloVariantGroupGui(int x, int y, String category, List<OutcomePreview> outcomes, int offset,
            Consumer<OutcomePreview> onVariantHover, Consumer<OutcomePreview> onVariantBlur, Consumer<OutcomePreview> onVariantSelect) {
        super(x, y, 0, 50);

        GuiString label = new GuiStringSmall(0, 0, I18n.format("tetra.variant_category." + category + ".label"));
        label.setColor(GuiColors.muted);
        addChild(label);

        variantsContainer = new GuiElement(0, 8, width, height);
        addChild(variantsContainer);

        labelAnimation = new KeyframeAnimation(100, label)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(x - 5, x))
                .withDelay(40 * offset);

        int width = 0;

        itemAnimations = new KeyframeAnimation[outcomes.size()];
        for (int i = 0; i < outcomes.size(); i++) {
            OutcomePreview outcome = outcomes.get(i);
            if (SchematicType.minor.equals(outcome.type)) {
                HoloVariantItemGui variant = new HoloVariantItemGui((i / 2) * 15, (i % 2) * 15, outcome,
                        onVariantHover, onVariantBlur, onVariantSelect);
                variantsContainer.addChild(variant);

                itemAnimations[i] = new KeyframeAnimation(80, variant)
                        .applyTo(new Applier.Opacity(0, 1),
                                new Applier.TranslateY(-5, 0, true))
                        .withDelay(40 + 40 * (offset + i / 2));

                width = variant.getX() + variant.getWidth();
            } else {
                HoloVariantMajorItemGui variant = new HoloVariantMajorItemGui((i / 2) * 20 + (i % 2) * 10, (i % 2) * 15, outcome,
                        onVariantHover, onVariantBlur, onVariantSelect);
                variantsContainer.addChild(variant);

                itemAnimations[i] = new KeyframeAnimation(80, variant)
                        .applyTo(new Applier.Opacity(0, 1),
                                new Applier.TranslateX(-5, 0, true),
                                new Applier.TranslateY(-5, 0, true))
                        .withDelay(40 + 40 * (offset + i / 2));

                width = variant.getX() + variant.getWidth() - (i % 2) * 5;
            }
        }

        setWidth(Math.max(width, label.getWidth()));
    }

    public void updateSelection(OutcomePreview outcome) {
        variantsContainer.getChildren(HoloVariantItemGui.class).forEach(variant -> variant.updateSelection(outcome));
    }

    public void animateIn() {
        labelAnimation.start();
        Arrays.stream(itemAnimations).forEach(KeyframeAnimation::start);
    }
}
