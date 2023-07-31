package se.mickelus.tetra.gui.stats.bar;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.blocks.workbench.gui.GuiTool;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiStatBarTool extends GuiStatBar {
    private static final int efficiencyMax = 50;

    private final GuiTool icon;
    private final IStatGetter levelGetter;

    private final boolean efficiencyVisibility;

    public GuiStatBarTool(int x, int y, int width, ToolAction toolAction) {
        this(x, y, width, toolAction, false, true);
    }

    public GuiStatBarTool(int x, int y, int width, ToolAction toolAction, boolean efficiencyVisibility, boolean includeSpeedModifier) {
        super(x, y, width, null, 0, efficiencyMax, false,
                includeSpeedModifier ? new StatGetterToolCompoundEfficiency(new StatGetterToolEfficiency(toolAction),
                        new StatGetterAttribute(Attributes.ATTACK_SPEED), new StatGetterEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1))
                        : new StatGetterSum(new StatGetterToolEfficiency(toolAction), new StatGetterEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1)),
                LabelGetterBasic.decimalLabel, new TooltipGetterTool(toolAction, includeSpeedModifier));

        this.efficiencyVisibility = efficiencyVisibility;

        bar.setWidth(width - 16);
        bar.setX(16);

        levelGetter = new StatGetterToolLevel(toolAction);
        icon = new GuiTool(-3, -3, toolAction);
        addChild(icon);

        IStatGetter extractionGetter = new StatGetterEffectLevel(ItemEffect.extraction, 4.5);
        IStatGetter unboundExtractionGetter = new StatGetterEffectLevel(ItemEffect.unboundExtraction, 1);
        IStatGetter enchantmentGetter = new StatGetterEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1);
        IStatGetter actionGetter = new StatGetterStriking(toolAction);
        IStatGetter sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1);
        IStatGetter truesweepGetter = new StatGetterEffectLevel(ItemEffect.truesweep, 1);
        IStatGetter planarSweepGetter = new StatGetterEffectLevel(ItemEffect.planarSweep, 1);
        IStatGetter focusGetter = new StatGetterEffectEfficiency(ItemEffect.sweepingFocus, 1);

        setIndicators(
                new StrikingStatIndicatorGui(toolAction),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.truesweepingStrike", 4, truesweepGetter,
                        new TooltipGetterNone("tetra.stats.tool.truesweepingStrike.tooltip")).withShowRequirements(actionGetter, sweepingGetter),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.planarSweep", 21, planarSweepGetter,
                        new TooltipGetterNone("tetra.stats.tool.planarSweep.tooltip")).withShowRequirements(actionGetter, sweepingGetter),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.sweepingFocus", 22, focusGetter,
                        new TooltipGetterSweepingFocus(focusGetter)).withShowRequirements(actionGetter, sweepingGetter),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.extraction", 7, extractionGetter,
                        new TooltipGetterInteger("tetra.stats.tool.extraction.tooltip", extractionGetter)),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.unboundExtraction", 20, unboundExtractionGetter,
                        new TooltipGetterInteger("tetra.stats.tool.unboundExtraction.tooltip", unboundExtractionGetter)),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.efficiency", 17, enchantmentGetter,
                        new TooltipGetterInteger("tetra.stats.tool.efficiency.tooltip", enchantmentGetter)));
    }

    @Override
    public void update(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        super.update(player, currentStack, previewStack, slot, improvement);

        int level = (int) levelGetter.getValue(player, currentStack);
        int color = GuiColors.normal;

        if (!previewStack.isEmpty()) {
            int previewLevel = (int) levelGetter.getValue(player, previewStack);

            color = getDiffColor(level, previewLevel);
            level = previewLevel;
        } else if (slot != null) {
            int previewLevel = level - getSlotLevel(player, currentStack, slot, improvement);

            color = getDiffColor(previewLevel, level);
        }

        icon.update(level, color);
    }

    @Override
    protected void realign() {
        super.realign();

        if (GuiAlignment.left.equals(alignment)) {
            bar.setX(16);
            icon.setX(-3);
        } else {
            bar.setX(0);
            icon.setX(0);
        }

        icon.setAttachment(alignment.toAttachment());

        int offset = icon.getWidth();
        indicatorGroup.setX(GuiAlignment.right.equals(alignment) ? -offset : offset);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        return levelGetter.getValue(player, currentStack) > 0 || levelGetter.getValue(player, previewStack) > 0
                || (efficiencyVisibility && (statGetter.getValue(player, currentStack) > 0 || statGetter.getValue(player, previewStack) > 0));
    }

    protected int getDiffColor(int currentValue, int previewValue) {
        if (previewValue > currentValue) {
            return GuiColors.positive;
        } else if (previewValue < currentValue) {
            return GuiColors.negative;
        }

        return GuiColors.normal;
    }

    protected int getSlotLevel(Player player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> improvement != null ? levelGetter.getValue(player, itemStack, slot, improvement) : levelGetter.getValue(player, itemStack, slot))
                .orElse(-1d)
                .intValue();
    }
}
