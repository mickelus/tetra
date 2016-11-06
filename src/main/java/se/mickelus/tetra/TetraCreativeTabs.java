package se.mickelus.tetra;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBook;

public class TetraCreativeTabs extends CreativeTabs {

    private static TetraCreativeTabs instance;

    public TetraCreativeTabs() {
        super("tetra");

        instance = this;
    }

    @Override
    public Item getTabIconItem() {
        return Items.ITEM_FRAME;
    }

    public static TetraCreativeTabs getInstance() {
        return instance;
    }

}
