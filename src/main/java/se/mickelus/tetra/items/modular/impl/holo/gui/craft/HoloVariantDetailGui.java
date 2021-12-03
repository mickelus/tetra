package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiItem;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.blocks.workbench.gui.ToolRequirementGui;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiSynergyIndicator;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.properties.PropertyHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class HoloVariantDetailGui extends GuiElement {

    private final GuiHorizontalLayoutGroup header;
    private GuiString variantLabel;

    private GuiSynergyIndicator synergyIndicator;

    private GuiElement requiredTools;
    private GuiItemRolling material;

    private HoloStatsGui stats;

    private Map<ToolAction, Integer> availableToolLevels;

    private Runnable populateImprovements;
    private HoloImprovementButton improvementButton;
    private HoloImprovementListGui improvements;

    private KeyframeAnimation openAnimation;
    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    private final KeyframeAnimation foldAnimation;
    private final KeyframeAnimation unfoldAnimation;

    private OutcomePreview variantOutcome;
    private OutcomePreview currentOutcome;
    private String slot;
    private List<OutcomeStack> selectedOutcomes;
    private OutcomePreview hoveredImprovement;

    private int originalY;

    public HoloVariantDetailGui(int x, int y, int width, Consumer<OutcomePreview> onVariantOpen) {
        super(x, y, width, 100);

        originalY = y;

        selectedOutcomes = new LinkedList<>();

        header = new GuiHorizontalLayoutGroup(0, 0, 20, 5);
        addChild(header);

        variantLabel = new GuiString(0, 0, "");
        header.addChild(variantLabel);

        synergyIndicator = new GuiSynergyIndicator(0, -1, true);
        header.addChild(synergyIndicator);

        GuiElement materialWrapper = new MaterialWrapper(0, -4);
        material = new GuiItemRolling(0, 0)
                .setCountVisibility(GuiItem.CountMode.always);
        materialWrapper.addChild(material);
        header.addChild(materialWrapper);

        requiredTools = new GuiElement(0, -3, width, height);
        header.addChild(requiredTools);

        Player player = Minecraft.getInstance().player;
        availableToolLevels = Stream.of(PropertyHelper.getPlayerToolLevels(player), PropertyHelper.getToolbeltToolLevels(player))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));

        stats = new HoloStatsGui(-5, 24);
        addChild(stats);

        improvementButton = new HoloImprovementButton(0, 64, () -> onVariantOpen.accept(variantOutcome));
        improvementButton.setAttachment(GuiAttachment.topCenter);
        addChild(improvementButton);

        improvements = new HoloImprovementListGui(0, 78, width, 0, this::onImprovementHover, this::onImprovementBlur, this::onImprovementSelect);
        addChild(improvements);

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

        foldAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.TranslateY(0));

        unfoldAnimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y));
    }

    public void updateVariant(OutcomePreview selectedOutcome, OutcomePreview hoveredOutcome, String slot) {
        variantOutcome = selectedOutcome;
        currentOutcome = selectedOutcome;
        this.slot = slot;

        if (selectedOutcome != null || hoveredOutcome != null) {
            OutcomePreview baseOutcome = hoveredOutcome != null ? hoveredOutcome : selectedOutcome;

            variantLabel.setString(I18n.get(ItemModule.getName(baseOutcome.moduleKey, baseOutcome.variantKey)));

            synergyIndicator.update(baseOutcome.itemStack, slot);

            Player player = Minecraft.getInstance().player;
            ItemStack improvementStack = baseOutcome.itemStack;
            UpgradeSchematic[] improvementSchematics = Arrays.stream(SchematicRegistry.getSchematics(slot, improvementStack))
                    .filter(improvementSchematic -> SchematicType.improvement.equals(improvementSchematic.getType()))
                    .filter(improvementSchematic -> improvementSchematic.isApplicableForItem(improvementStack))
                    .filter(improvementSchematic -> improvementSchematic.isVisibleForPlayer(player, null, improvementStack))
                    .toArray(UpgradeSchematic[]::new);

            improvementButton.updateCount(improvementSchematics.length);

            populateImprovements = () -> {
                improvements.updateSchematics(improvementStack, slot, improvementSchematics);
                populateImprovements = null;
            };

            requiredTools.clearChildren();
            baseOutcome.tools.getLevelMap().forEach((tool, level) -> {
                ToolRequirementGui requirement = new ToolRequirementGui( requiredTools.getNumChildren() * 20, 0, tool,
                        "tetra.tool." + tool.getName() + ".craft_requirement");
                requirement.updateRequirement(level, availableToolLevels.getOrDefault(tool, 0));
                requiredTools.addChild(requirement);
            });

            material.setItems(baseOutcome.materials);

            updateStats(selectedOutcome, hoveredOutcome);

            header.forceLayout();

            show();
        } else {
            hide();
        }
    }

    public void onImprovementSelect(OutcomeStack selectedStack) {
        boolean wasRemoved = selectedOutcomes.removeIf(stack -> stack.equals(selectedStack));

        if (!wasRemoved) {
            selectedOutcomes.add(selectedStack);
        }

        currentOutcome = variantOutcome.clone();

        for (OutcomeStack stack : selectedOutcomes) {
            OutcomePreview[] tempPreviews = stack.schematic.getPreviews(currentOutcome.itemStack, slot);
            for (OutcomePreview tempPreview : tempPreviews) {
                if (tempPreview.equals(stack.preview)) {
                    currentOutcome = tempPreview;
                    break;
                }
            }
        }

        selectedOutcomes.removeIf(stack -> !stack.preview.isApplied(currentOutcome.itemStack, slot));

        improvements.updateSelection(currentOutcome.itemStack, selectedOutcomes);

        updateStats(currentOutcome, currentOutcome);
    }

    private void onImprovementHover(OutcomePreview improvement) {
        updateStats(currentOutcome, improvement);
        hoveredImprovement = improvement;
    }

    private void onImprovementBlur(OutcomePreview improvement) {
        if (improvement.equals(hoveredImprovement)) {
            updateStats(currentOutcome, null);
            hoveredImprovement = null;
        }
    }

    public void updateStats(OutcomePreview selectedOutcome, OutcomePreview hoveredOutcome) {
        ItemStack baseStack = hoveredOutcome != null ? hoveredOutcome.itemStack : selectedOutcome != null ? selectedOutcome.itemStack : ItemStack.EMPTY;
        stats.update(selectedOutcome != null ? selectedOutcome.itemStack : baseStack, baseStack,null, null,
                Minecraft.getInstance().player);
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

    public void forceHide() {
        setY(originalY);
        setOpacity(0);
        improvements.forceHide();
        improvementButton.setVisible(false);
    }

    public void showImprovements() {
        if (populateImprovements != null) {
            populateImprovements.run();
        }

        unfoldAnimation.stop();
        foldAnimation.start();
        improvements.show();
        improvementButton.hide();
    }

    public void hideImprovements() {
        currentOutcome = variantOutcome;
        selectedOutcomes.clear();

        if (currentOutcome != null) {
            updateStats(currentOutcome, null);
        }

        foldAnimation.stop();
        unfoldAnimation.start();

        improvements.hide();
        improvementButton.show();
    }

    static class MaterialWrapper extends GuiElement {
        public MaterialWrapper(int x, int y) {
            super(x, y, 16, 16);
        }

        @Override
        public List<String> getTooltipLines() {
            if (hasFocus()) {
                List<String> tooltip = super.getTooltipLines();
                if (tooltip != null && tooltip.size() > 0) {
                    return ImmutableList.of(I18n.get("tetra.holo.craft.material_requirement", tooltip.get(0)));
                }
            }
            return null;
        }
    }
}
