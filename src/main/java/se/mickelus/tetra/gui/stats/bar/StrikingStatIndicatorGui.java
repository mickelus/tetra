package se.mickelus.tetra.gui.stats.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import se.mickelus.tetra.gui.stats.getter.TooltipGetterNone;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class StrikingStatIndicatorGui extends GuiStatIndicator {
    GuiStatIndicator striking;
    GuiStatIndicator sweeping;
    GuiStatIndicator current;

    public StrikingStatIndicatorGui(ToolAction toolAction) {
        super(0, 0, "", 0, null, null);

        if (toolAction == ToolAction.AXE) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingAxe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolAction == ToolAction.PICKAXE) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingPickaxe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolAction == TetraToolActions.cut) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingCut, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolAction == ToolAction.SHOVEL) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingShovel, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        } else if (toolAction == ToolAction.HOE) {
            striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                    new StatGetterEffectLevel(ItemEffect.strikingHoe, 1), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));
        }

        sweeping = new GuiStatIndicator(0, 0, "tetra.stats.tool.sweeping", 1,
                new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1),
                new TooltipGetterNone("tetra.stats.tool.sweeping.tooltip"));
    }

    public boolean update(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
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
    public boolean isActive(Player player, ItemStack itemStack) {
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

    public String getTooltipBase(Player player, ItemStack itemStack) {
        return Optional.ofNullable(current)
                .map(c -> c.getTooltipBase(player, itemStack))
                .orElse("");
    }

    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return Optional.ofNullable(current)
                .map(c -> c.hasExtendedTooltip(player, itemStack))
                .orElse(false);
    }

    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return Optional.ofNullable(current)
                .map(c -> c.getTooltipExtension(player, itemStack))
                .orElse("");
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (current != null) {
            current.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }
}
