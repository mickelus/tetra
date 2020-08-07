package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.function.Consumer;

public class HoloVariantsGui extends GuiElement {

    private GuiElement variantsContainer;

    private HoloVariantItemGui[] variants;

    private KeyframeAnimation labelAnimation;

    private KeyframeAnimation[] itemAnimations;

    private Consumer<OutcomePreview> onVariantHover;
    private Consumer<OutcomePreview> onVariantBlur;
    private Consumer<OutcomePreview> onVariantSelect;

    public HoloVariantsGui(int x, int y, int width, Consumer<OutcomePreview> onVariantHover, Consumer<OutcomePreview> onVariantBlur,
            Consumer<OutcomePreview> onVariantSelect) {
        super(x, y, width, 50);

        GuiString variantsLabel = new GuiStringSmall(0, 0, I18n.format("tetra.holo.craft.variants"));
        variantsLabel.setColor(GuiColors.muted);
        addChild(variantsLabel);

        variants = new HoloVariantItemGui[0];

        variantsContainer = new GuiElement(0, 8, width, height);
        addChild(variantsContainer);

        labelAnimation = new KeyframeAnimation(100, variantsLabel)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(x - 5, x));

        itemAnimations = new KeyframeAnimation[0];

        this.onVariantHover = onVariantHover;
        this.onVariantBlur = onVariantBlur;
        this.onVariantSelect = onVariantSelect;
    }

    public void update(ModularItem item, String slot, UpgradeSchematic schematic) {
        variantsContainer.clearChildren();

        OutcomePreview[] outcomes = Arrays.stream(schematic.getPreviews(new ItemStack(item), slot))
                .filter(preview -> preview.materials.length != 0)
                .toArray(OutcomePreview[]::new);

        variants = new HoloVariantItemGui[outcomes.length];
        itemAnimations = new KeyframeAnimation[outcomes.length];
        for (int i = 0; i < outcomes.length; i++) {
            if (SchematicType.minor.equals(outcomes[i].type)) {
                variants[i] = new HoloVariantItemGui((i / 2) * 15, (i % 2) * 15, outcomes[i],
                        onVariantHover, onVariantBlur, onVariantSelect);
                variantsContainer.addChild(variants[i]);

                itemAnimations[i] = new KeyframeAnimation(80, variants[i])
                        .applyTo(new Applier.Opacity(0, 1),
                                new Applier.TranslateY(-5, 0, true))
                        .withDelay(40 + 40 * (i / 2));
            } else {
                variants[i] = new HoloVariantMajorItemGui((i / 2) * 20 + (i % 2) * 10, (i % 2) * 15, outcomes[i],
                        onVariantHover, onVariantBlur, onVariantSelect);
                variantsContainer.addChild(variants[i]);

                itemAnimations[i] = new KeyframeAnimation(80, variants[i])
                        .applyTo(new Applier.Opacity(0, 1),
                                new Applier.TranslateX(-5, 0, true),
                                new Applier.TranslateY(-5, 0, true))
                        .withDelay(40 + 40 * (i / 2));
            }
        }
    }

    public void updateSelection(OutcomePreview outcome) {
        for (HoloVariantItemGui variant: variants) {
            variant.updateSelection(outcome);
        }
    }

    @Override
    protected void onShow() {
        labelAnimation.start();
        Arrays.stream(itemAnimations).forEach(KeyframeAnimation::start);
    }
}
