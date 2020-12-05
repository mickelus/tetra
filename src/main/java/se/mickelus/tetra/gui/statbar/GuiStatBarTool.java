package se.mickelus.tetra.gui.statbar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.workbench.gui.GuiTool;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.util.CastOptional;

public class GuiStatBarTool extends GuiStatBar {
    private static final int efficiencyMax = 50;

    private GuiTool icon;
    private IStatGetter levelGetter;

    private GuiStatIndicator strikingIndicator;
    private GuiStatIndicator sweepingIndicator;

    private boolean efficiencyVisibility;

    public GuiStatBarTool(int x, int y, int width, ToolType toolType) {
        this(x, y, width, toolType, false);
    }

    public GuiStatBarTool(int x, int y, int width, ToolType toolType, boolean efficiencyVisibility) {
        super(x, y, width, "", 0, efficiencyMax,
                false, new StatGetterToolEfficiency(toolType), LabelGetterBasic.decimalLabel,
                new TooltipGetterTool(toolType));

        this.efficiencyVisibility = efficiencyVisibility;

        bar.setWidth(width - 16);
        bar.setX(16);

        levelGetter = new StatGetterToolLevel(toolType);
        icon = new GuiTool(-3, -3, toolType);
        addChild(icon);

        sweepingIndicator = new GuiStatIndicator(0, 0, "tetra.stats.tool.sweeping", 1,
                new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1), new TooltipGetterNone("tetra.stats.tool.sweeping.tooltip"));

        if (toolType == ToolType.AXE) {
            strikingIndicator = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingAxe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolType.PICKAXE) {
            strikingIndicator = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingPickaxe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolTypes.cut) {
            strikingIndicator = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingCut, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolType.SHOVEL) {
            strikingIndicator = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingShovel, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolType.HOE) {
            strikingIndicator = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingHoe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        }
    }

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
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

    protected void updateIndicators(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        indicatorGroup.clearChildren();

        if (strikingIndicator != null && strikingIndicator.update(player, currentStack, previewStack, slot, improvement)) {
            if (sweepingIndicator.update(player, currentStack, previewStack, slot, improvement)) {
                indicatorGroup.addChild(sweepingIndicator);
            } else {
                indicatorGroup.addChild(strikingIndicator);
            }
        }
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

        int offset = icon.getWidth() + 2;
        indicatorGroup.setX(GuiAlignment.right.equals(alignment) ? -offset : offset);
    }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
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

    protected int getSlotLevel(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> {
                    if (improvement != null) {
                        return levelGetter.getValue(player, itemStack, slot, improvement);
                    }

                    return levelGetter.getValue(player, itemStack, slot);
                })
                .orElse(-1d).intValue();
    }
}
