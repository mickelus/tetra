package se.mickelus.tetra.gui.statbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.IStatGetter;
import se.mickelus.tetra.gui.statbar.getter.ITooltipGetter;

import java.util.Collections;

public class GuiStatIndicator extends GuiTexture {
    protected String label;
    protected IStatGetter statGetter;
    protected ITooltipGetter tooltipGetter;

    public GuiStatIndicator(int x, int y, String label, int textureIndex, IStatGetter statGetter, ITooltipGetter tooltipGetter) {
        super(x, y, 7, 7, textureIndex * 7, 144, GuiTextures.workbench);

        this.label = I18n.format(label);
        this.statGetter = statGetter;
        this.tooltipGetter = tooltipGetter;
    }

    /**
     * Updates the indicator
     * @param player
     * @param currentStack
     * @param previewStack
     * @param slot
     * @param improvement
     * @return true if the indicator should be displayed
     */
    public boolean update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        double value;
        double diffValue;

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

        if (value != 0 || diffValue != 0) {
            setColor(getDiffColor(value, diffValue));
            return true;
        }

        return false;
    }

    protected int getDiffColor(double value, double diffValue) {
        if (diffValue > 0 && value <= 0) {
            return GuiColors.positive;
        } else if (diffValue <= 0 && value > 0) {
            return GuiColors.negative;
        } else if (diffValue == value) {
            return GuiColors.normal;
        }

        return GuiColors.change;
    }

    public String getLabel() {
        return label;
    }

    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return tooltipGetter.getTooltipBase(player, itemStack);
    }

    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return tooltipGetter.hasExtendedTooltip(player, itemStack);
    }

    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return tooltipGetter.getTooltipExtension(player, itemStack);
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
//        new GuiRect(x, y, 8, 8, GuiColors.normal).draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
    }
}
