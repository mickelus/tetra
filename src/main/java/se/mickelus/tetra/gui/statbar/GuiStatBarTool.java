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
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.util.CastOptional;

public class GuiStatBarTool extends GuiStatBar {
    private static final int efficiencyMax = 50;

    private GuiTool icon;
    private IStatGetter levelGetter;

    private GuiTexture strikingIndicator;
    private IStatGetter strikingGetter;

    private GuiTexture sweepingIndicator;
    private IStatGetter sweepingGetter;

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

        if (toolType == ToolType.AXE) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingAxe, 1);
        } else if (toolType == ToolType.PICKAXE) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingPickaxe, 1);
        } else if (toolType == ToolTypes.cut) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingCut, 1);
        } else if (toolType == ToolType.SHOVEL) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingShovel, 1);
        } else if (toolType == ToolType.HOE) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingHoe, 1);
        }

        strikingIndicator = new GuiTexture(16, -1, 7, 7, 206, 0, GuiTextures.workbench);
        addChild(strikingIndicator);

        sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1);
        sweepingIndicator = new GuiTexture(16, -1, 7, 7, 213, 0, GuiTextures.workbench);
        addChild(sweepingIndicator);
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

        updateIndicators(player, currentStack, previewStack, slot, improvement);
    }

    private void updateIndicators(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        int currentStriking = strikingGetter != null ? (int) strikingGetter.getValue(player, currentStack) : 0;
        int previewStriking = currentStriking;

        int currentSweeping = (int) sweepingGetter.getValue(player, currentStack);
        int previewSweeping = currentSweeping;

        if (!previewStack.isEmpty()) {
            previewStriking = strikingGetter != null ? (int) strikingGetter.getValue(player, previewStack) : 0;
            previewSweeping = (int) sweepingGetter.getValue(player, previewStack);
        } else if (slot != null) {
            if (improvement != null) {
                previewStriking = strikingGetter != null ? (int) strikingGetter.getValue(player, currentStack, slot, improvement) : 0;
                previewSweeping = (int) sweepingGetter.getValue(player, currentStack, slot, improvement);
            } else {
                previewStriking = strikingGetter != null ? (int) strikingGetter.getValue(player, currentStack, slot) : 0;
                previewSweeping = (int) sweepingGetter.getValue(player, currentStack, slot);
            }
            currentStriking -= previewStriking;
            currentSweeping -= previewSweeping;
        }

        if (currentStriking > 0 || previewStriking > 0) {
            if (currentSweeping > 0 || previewSweeping > 0) {
                strikingIndicator.setVisible(false);
                sweepingIndicator.setVisible(true);

                sweepingIndicator.setColor(getDiffColor(currentSweeping, previewSweeping));
            } else {
                strikingIndicator.setVisible(true);
                sweepingIndicator.setVisible(false);

                strikingIndicator.setColor(getDiffColor(currentStriking, previewStriking));
            }
        } else {
            strikingIndicator.setVisible(false);
            sweepingIndicator.setVisible(false);
        }

    }

    @Override
    protected void realign() {
        super.realign();

        if (GuiAlignment.left.equals(alignment)) {
            bar.setX(16);
            icon.setX(-3);

            strikingIndicator.setX(16);
            sweepingIndicator.setX(16);
        } else {
            bar.setX(0);
            icon.setX(0);

            strikingIndicator.setX(-16);
            sweepingIndicator.setX(-16);
        }

        strikingIndicator.setAttachment(alignment.toAttachment());
        sweepingIndicator.setAttachment(alignment.toAttachment());
        icon.setAttachment(alignment.toAttachment());
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
