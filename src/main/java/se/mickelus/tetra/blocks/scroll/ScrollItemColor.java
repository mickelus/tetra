package se.mickelus.tetra.blocks.scroll;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ScrollItemColor implements ItemColor {
    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (tintIndex == 1) {
            return ScrollData.readRibbonFast(itemStack);
        }
        return 0xffffff;
    }
}
