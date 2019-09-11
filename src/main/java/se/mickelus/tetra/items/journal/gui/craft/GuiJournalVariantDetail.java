package se.mickelus.tetra.items.journal.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.gui.GuiCapabilityRequirement;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.OutcomePreview;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.Arrays;
import java.util.Map;

public class GuiJournalVariantDetail extends GuiElement {

    private GuiString variantLabel;

    private GuiString improvementsLabel;
    private GuiElement improvements;

    private GuiElement requiredCapabilities;
    private GuiItem material;

    private GuiJournalStats stats;

    private int[] capabilityLevels;

    private KeyframeAnimation openAnimation;
    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public GuiJournalVariantDetail(int x, int y, int width) {
        super(x, y, width, 100);

        variantLabel = new GuiString(0, 0, "");
        addChild(variantLabel);

        // variant requirements
        GuiStringSmall requirementsLabel = new GuiStringSmall(0, 13, I18n.format("journal.craft.requirements"));
        requirementsLabel.setColor(GuiColors.muted);
        addChild(requirementsLabel);

        requiredCapabilities = new GuiElement(0, 20, width, height);
        addChild(requiredCapabilities);

        material = new GuiItem(0, 20);
        addChild(material);

        EntityPlayer player = Minecraft.getMinecraft().player;
        capabilityLevels = new int[Capability.values().length];
        for (int i = 0; i < capabilityLevels.length; i++) {
            capabilityLevels[i] = CapabilityHelper.getPlayerCapabilityLevel(player, Capability.values()[i]);
        }

        // variant improvements
        improvementsLabel = new GuiStringSmall(57, 13, I18n.format("journal.craft.improvements"));
        improvementsLabel.setColor(GuiColors.muted);
        addChild(improvementsLabel);

        improvements = new GuiElement(57, 18, width, height);
        addChild(improvements);

        // variant stats
        GuiStringSmall statsLabel = new GuiStringSmall(120, 13, I18n.format("journal.craft.stats"));
        statsLabel.setColor(GuiColors.muted);
        addChild(statsLabel);

        stats = new GuiJournalStats(120, 20);
        addChild(stats);

        // animations
        openAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(y - 4, y))
                .withDelay(120);

        showAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y));

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 5))
                .onStop(complete -> {
                    if (complete) {
                        this.isVisible = false;
                    }
                });
    }

    public void updateVariant(OutcomePreview selectedOutcome, OutcomePreview hoveredOutcome, String slot) {
        if (selectedOutcome != null || hoveredOutcome != null) {
            OutcomePreview baseOutcome = hoveredOutcome != null ? hoveredOutcome : selectedOutcome;

            variantLabel.setString(I18n.format(baseOutcome.key));

            ItemStack improvementStack = baseOutcome.itemStack;
            UpgradeSchema[] improvementSchemas = Arrays.stream(ItemUpgradeRegistry.instance.getSchemas(slot))
                    .filter(improvementSchema -> SchemaType.improvement.equals(improvementSchema.getType()))
                    .filter(improvementSchema -> improvementSchema.isApplicableForItem(improvementStack))
                    .toArray(UpgradeSchema[]::new);

            improvementsLabel.setVisible(improvementSchemas.length > 0);
            improvements.setVisible(improvementSchemas.length > 0);
            if (improvementSchemas.length > 0) {
                improvements.clearChildren();
                for (int i = 0; i < improvementSchemas.length; i++) {
                    improvements.addChild(new GuiJournalImprovement(0, i * 20, improvementSchemas[i]));
                }
            }

            requiredCapabilities.clearChildren();
            int i = 0;
            for (Map.Entry<Capability, Integer> entry: baseOutcome.capabilities.valueMap.entrySet()) {
                GuiCapabilityRequirement requirement = new GuiCapabilityRequirement(20, i * 18, entry.getKey());
                requirement.updateRequirement(entry.getValue(), capabilityLevels[entry.getKey().ordinal()]);
                requiredCapabilities.addChild(requirement);

                i++;
            }
            if (baseOutcome.materials.length > 0) {
                material.setItem(baseOutcome.materials[0]);
            } else {
                material.setItem(null);
            }

            stats.update(selectedOutcome != null ? selectedOutcome.itemStack : hoveredOutcome.itemStack,
                    baseOutcome.itemStack,null, null, Minecraft.getMinecraft().player);

            show();
        } else {
            hide();
        }
    }

    public void animateOpen() {
        openAnimation.start();
    }

    public void show() {
        hideAnimation.stop();
        setVisible(true);
        showAnimation.start();
    }

    public void hide() {
        showAnimation.stop();
        hideAnimation.start();
    }
}
