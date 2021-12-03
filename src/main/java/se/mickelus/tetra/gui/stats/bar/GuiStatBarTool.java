package se.mickelus.tetra.gui.stats.bar;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.tetra.blocks.workbench.gui.GuiTool;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class GuiStatBarTool extends GuiStatBar {
    private static final int efficiencyMax = 50;

    private GuiTool icon;
    private IStatGetter levelGetter;

    private boolean efficiencyVisibility;

    public GuiStatBarTool(int x, int y, int width, ToolAction toolAction) {
        this(x, y, width, toolAction, false);
    }

    public GuiStatBarTool(int x, int y, int width, ToolAction toolAction, boolean efficiencyVisibility) {
        super(x, y, width, null, 0, efficiencyMax,
                false, new StatGetterToolEfficiency(toolAction), LabelGetterBasic.decimalLabel,
                new TooltipGetterTool(toolAction));

        this.efficiencyVisibility = efficiencyVisibility;

        bar.setWidth(width - 16);
        bar.setX(16);

        levelGetter = new StatGetterToolLevel(toolAction);
        icon = new GuiTool(-3, -3, toolAction);
        addChild(icon);

        StatGetterEffectLevel extractorGetter = new StatGetterEffectLevel(ItemEffect.extractor, 4.5);
        setIndicators(
                new StrikingStatIndicatorGui(toolAction),
                new GuiStatIndicator(0, 0, "tetra.stats.tool.extractor", 7, extractorGetter,
                        new TooltipGetterInteger("tetra.stats.tool.extractor.tooltip", extractorGetter)));
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

        int offset = icon.getWidth() + 2;
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
                .map(item -> {
                    if (improvement != null) {
                        return levelGetter.getValue(player, itemStack, slot, improvement);
                    }

                    return levelGetter.getValue(player, itemStack, slot);
                })
                .orElse(-1d).intValue();
    }
}
