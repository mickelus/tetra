package se.mickelus.tetra.blocks.scroll;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class ScrollItemColor implements IItemColor {
    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (tintIndex == 1) {
            return ScrollData.readRibbonFast(itemStack);
        }
        return 0xffffff;
    }
}
