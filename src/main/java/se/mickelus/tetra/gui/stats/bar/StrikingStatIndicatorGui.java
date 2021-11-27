package se.mickelus.tetra.gui.stats.bar;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.stats.getter.*;

import java.util.Optional;

/**
 * Displays the first of the provided indicators that is active
 */
public class StrikingStatIndicatorGui extends GuiStatIndicator {
    GuiStatIndicator striking;
    GuiStatIndicator sweeping;
    GuiStatIndicator current;

    public StrikingStatIndicatorGui(ToolType toolType) {
        super(0, 0, "", 0, null, null);

        if (toolType == ToolType.AXE) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingAxe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolType.PICKAXE) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingPickaxe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolTypes.cut) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingCut, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolType.SHOVEL) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingShovel, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolType == ToolType.HOE) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingHoe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        }

        sweeping = new GuiStatIndicator(0, 0, "tetra.stats.tool.sweeping", 1,
                new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1),
                new TooltipGetterNone("tetra.stats.tool.sweeping.tooltip"));
    }

    public boolean update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        if (striking != null && striking.update(player, currentStack, previewStack, slot, improvement)) {
            if (sweeping.update(player, currentStack, previewStack, slot, improvement)) {
                current = sweeping;
            } else {
                current = striking;
            }
            return true;
        }

        current = null;
        return false;
    }

    @Override
    public boolean isActive(PlayerEntity player, ItemStack itemStack) {
        return current != null && current.isActive(player, itemStack);
    }

    protected int getDiffColor(double value, double diffValue) {
        return Optional.ofNullable(current)
                .map(c -> c.getDiffColor(value, diffValue))
                .orElse(GuiColors.normal);
    }

    public String getLabel() {
        return Optional.ofNullable(current)
                .map(c -> c.label)
                .orElse("");
    }

    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return Optional.ofNullable(current)
                .map(c -> c.getTooltipBase(player, itemStack))
                .orElse("");
    }

    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return Optional.ofNullable(current)
                .map(c -> c.hasExtendedTooltip(player, itemStack))
                .orElse(false);
    }

    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return Optional.ofNullable(current)
                .map(c -> c.getTooltipExtension(player, itemStack))
                .orElse("");
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (current != null) {
            current.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }
}
