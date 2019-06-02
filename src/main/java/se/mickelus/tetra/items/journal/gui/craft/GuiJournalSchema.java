package se.mickelus.tetra.items.journal.gui.craft;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.OutcomePreview;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.Arrays;

public class GuiJournalSchema extends GuiElement {

    private GuiElement variants;

    private GuiString improvementsLabel;
    private GuiElement improvements;

    private GuiString focusLabel;

    public GuiJournalSchema(int x, int y, int width, int height) {
        super(x, y, width, height);

        GuiString variantsLabel = new GuiString(0, 5, "Variants");
        variantsLabel.setColor(GuiColors.muted);
        addChild(variantsLabel);

        variants = new GuiElement(0, 20, width, height);
        addChild(variants);

        improvementsLabel = new GuiString(0, 70, "Improvements");
        improvementsLabel.setColor(GuiColors.muted);
        addChild(improvementsLabel);

        improvements = new GuiElement(0, 90, width, height);
        addChild(improvements);

        focusLabel = new GuiString(0, 140, "");
        addChild(focusLabel);
    }

    public void update(ItemModular item, String slot, UpgradeSchema schema) {
        variants.clearChildren();

        OutcomePreview[] outcomes = schema.getPreviews(new ItemStack(item), slot);

        for (int i = 0; i < outcomes.length; i++) {
            if (SchemaType.minor.equals(outcomes[i].type)) {
                variants.addChild(new GuiJournalVariant((i / 2) * 15, (i % 2) * 15, outcomes[i],
                        this::onVariantHover, this::onVariantSelect));
            } else {
                variants.addChild(new GuiJournalVariantMajor((i / 2) * 20, (i % 2) * 20, outcomes[i],
                        this::onVariantHover, this::onVariantSelect));
            }
        }

        improvementsLabel.setVisible(false);
        improvements.setVisible(false);
        if (outcomes.length > 0) {
            improvements.clearChildren();
            ItemStack improvementStack = outcomes[0].itemStack;
            UpgradeSchema[] improvementSchemas = Arrays.stream(ItemUpgradeRegistry.instance.getSchemas(slot))
                    .filter(improvementSchema -> SchemaType.improvement.equals(improvementSchema.getType()))
                    .filter(improvementSchema -> improvementSchema.isApplicableForItem(improvementStack))
                    .toArray(UpgradeSchema[]::new);

            if (improvementSchemas.length > 0) {
                for (int i = 0; i < improvementSchemas.length; i++) {
                    improvements.addChild(new GuiModuleGlyph((i / 2) * 20, (i % 2) * 20, 16, 16,
                            improvementSchemas[i].getGlyph()).setShift(false));
                }

                improvementsLabel.setVisible(true);
                improvements.setVisible(true);
            }
        }
    }

    private void onVariantHover(OutcomePreview outcome) {
        if (outcome != null) {
            focusLabel.setString(I18n.format(outcome.key));
        } else {
            focusLabel.setString("");
        }
    }

    private void onVariantSelect(OutcomePreview outcome) {

    }
}
