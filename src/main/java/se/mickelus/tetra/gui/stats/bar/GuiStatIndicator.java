package se.mickelus.tetra.gui.stats.bar;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiStatIndicator extends GuiTexture {
    protected String label;
    protected IStatGetter statGetter;
    protected ITooltipGetter tooltipGetter;

    public GuiStatIndicator(int x, int y, String label, int textureIndex, IStatGetter statGetter, ITooltipGetter tooltipGetter) {
        super(x, y, 7, 7, textureIndex * 7, 144, GuiTextures.workbench);

        this.label = I18n.get(label);
        this.statGetter = statGetter;
        this.tooltipGetter = tooltipGetter;
    }

    /**
     * Updates the indicator
     *
     * @param player
     * @param currentStack
     * @param previewStack
     * @param slot
     * @param improvement
     * @return true if the indicator should be displayed
     */
    public boolean update(Player player, ItemStack currentStack, ItemStack previewStack, @Nullable String slot, @Nullable String improvement) {
        double value;
        double diffValue;

        if (statGetter.shouldShow(player, currentStack, previewStack)) {
            if (!previewStack.isEmpty()) {
                value = statGetter.getValue(player, currentStack);
                diffValue = statGetter.getValue(player, previewStack);
            } else {
                value = statGetter.getValue(player, currentStack);

                if (slot != null) {
                    diffValue = value;
                    if (improvement != null) {
                        value = value - statGetter.getValue(player, currentStack, slot, improvement);
                    } else {
                        value = value - statGetter.getValue(player, currentStack, slot);
                    }
                } else {
                    diffValue = value;
                }
            }


            double baseValue = statGetter.getValue(player, ItemStack.EMPTY);
            setColor(getDiffColor(baseValue, value, diffValue));
            return true;
        }
        return false;
    }

    public boolean isActive(Player player, ItemStack itemStack) {
        return statGetter.shouldShow(player, itemStack, itemStack);
    }

    protected int getDiffColor(double baseValue, double value, double diffValue) {
        if (diffValue > baseValue && value <= baseValue) {
            return GuiColors.positive;
        } else if (diffValue <= baseValue && value > baseValue) {
            return GuiColors.negative;
        } else if (diffValue == value) {
            return GuiColors.normal;
        }

        return GuiColors.change;
    }

    public String getLabel() {
        return label;
    }

    public String getTooltipBase(Player player, ItemStack itemStack) {
        return tooltipGetter.getTooltipBase(player, itemStack);
    }

    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return tooltipGetter.hasExtendedTooltip(player, itemStack);
    }

    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return tooltipGetter.getTooltipExtension(player, itemStack);
    }
}
