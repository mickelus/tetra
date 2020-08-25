package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiItem;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.tetra.blocks.workbench.gui.ToolRequirementGui;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiSynergyIndicator;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HoloVariantDetailGui extends GuiElement {

    private GuiString variantLabel;

    private GuiSynergyIndicator synergyIndicator;

    private GuiString improvementsLabel;
    private GuiElement improvements;

    private GuiElement requiredTools;
    private GuiItemRolling material;

    private HoloStatsGui stats;

    private Map<ToolType, Integer> availableToolLevels;

    private KeyframeAnimation openAnimation;
    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public HoloVariantDetailGui(int x, int y, int width) {
        super(x, y, width, 100);

        variantLabel = new GuiString(0, 0, "");
        addChild(variantLabel);

        synergyIndicator = new GuiSynergyIndicator(0, -1, true);
        addChild(synergyIndicator);

        // variant requirements
        GuiStringSmall requirementsLabel = new GuiStringSmall(0, 13, I18n.format("tetra.holo.craft.requirements"));
        requirementsLabel.setColor(GuiColors.muted);
        addChild(requirementsLabel);

        requiredTools = new GuiElement(0, 20, width, height);
        addChild(requiredTools);

        material = new GuiItemRolling(0, 20)
                .setCountVisibility(GuiItem.CountMode.always);
        addChild(material);

        PlayerEntity player = Minecraft.getInstance().player;
        availableToolLevels = Stream.of(PropertyHelper.getPlayerToolLevels(player), PropertyHelper.getToolbeltToolLevels(player))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));


        // variant improvements
        improvementsLabel = new GuiStringSmall(57, 13, I18n.format("tetra.holo.craft.improvements"));
        improvementsLabel.setColor(GuiColors.muted);
        addChild(improvementsLabel);

        improvements = new GuiElement(57, 18, width, height);
        addChild(improvements);

        // variant stats
        GuiStringSmall statsLabel = new GuiStringSmall(120, 13, I18n.format("tetra.holo.craft.stats"));
        statsLabel.setColor(GuiColors.muted);
        addChild(statsLabel);

        stats = new HoloStatsGui(120, 20);
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

            variantLabel.setString(I18n.format("tetra.variant." + baseOutcome.key));

            synergyIndicator.setX(variantLabel.getWidth() + 4);
            synergyIndicator.update(baseOutcome.itemStack, slot);

            ItemStack improvementStack = baseOutcome.itemStack;
            UpgradeSchematic[] improvementSchematics = Arrays.stream(SchematicRegistry.getSchematics(slot))
                    .filter(improvementSchematic -> SchematicType.improvement.equals(improvementSchematic.getType()))
                    .filter(improvementSchematic -> improvementSchematic.isApplicableForItem(improvementStack))
                    .toArray(UpgradeSchematic[]::new);

            improvementsLabel.setVisible(improvementSchematics.length > 0);
            improvements.setVisible(improvementSchematics.length > 0);
            if (improvementSchematics.length > 0) {
                improvements.clearChildren();
                for (int i = 0; i < improvementSchematics.length; i++) {
                    improvements.addChild(new HoloImprovementGui(0, i * 18, improvementSchematics[i]));
                }
            }

            requiredTools.clearChildren();
            baseOutcome.tools.levelMap.forEach((tool, level) -> {
                ToolRequirementGui requirement = new ToolRequirementGui(20, requiredTools.getNumChildren() * 18, tool);
                requirement.updateRequirement(level, availableToolLevels.getOrDefault(tool, 0));
                requiredTools.addChild(requirement);
            });

            if (baseOutcome.materials.length > 0) {
                material.setItems(baseOutcome.materials);
            } else {
                material.setItems(new ItemStack[0]);
            }

            stats.update(selectedOutcome != null ? selectedOutcome.itemStack : hoveredOutcome.itemStack,
                    baseOutcome.itemStack,null, null, Minecraft.getInstance().player);

            show();
        } else {
            hide();
        }
    }

    public void animateOpen() {
        stats.realignBars();
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
