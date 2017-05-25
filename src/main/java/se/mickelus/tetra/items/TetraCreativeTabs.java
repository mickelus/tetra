package se.mickelus.tetra.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.toolbelt.ItemToolbelt;

public class TetraCreativeTabs extends CreativeTabs {

    private static TetraCreativeTabs instance;

    public TetraCreativeTabs() {
        super("tetra");

        instance = this;
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ItemToolbelt.instance);
    }

    public static TetraCreativeTabs getInstance() {
        return instance;
    }

}
