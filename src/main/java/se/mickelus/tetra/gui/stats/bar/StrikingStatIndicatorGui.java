package se.mickelus.tetra.gui.stats.bar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import se.mickelus.tetra.gui.stats.getter.StatGetterStriking;
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

        striking = new GuiStatIndicator(0, 0, "tetra.stats.tool.striking", 0,
                new StatGetterStriking(toolAction), new TooltipGetterNone("tetra.stats.tool.striking.tooltip"));

        sweeping = new GuiStatIndicator(0, 0, "tetra.stats.tool.sweeping", 1,
                new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1),
                new TooltipGetterNone("tetra.stats.tool.sweeping.tooltip"));
    }

    @Override
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

    @Override
    protected int getDiffColor(double baseValue, double value, double diffValue) {
        return Optional.ofNullable(current)
                .map(c -> c.getDiffColor(baseValue, value, diffValue))
                .orElse(GuiColors.normal);
    }

    public String getLabel() {
        return Optional.ofNullable(current)
                .map(GuiStatIndicator::getLabel)
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
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (current != null) {
            current.draw(graphics, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }
}
