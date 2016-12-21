package se.mickelus.tetra.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import se.mickelus.tetra.items.toolbelt.ItemToolbelt;

public class TetraCreativeTabs extends CreativeTabs {

    private static TetraCreativeTabs instance;

    public TetraCreativeTabs() {
        super("tetra");

        instance = this;
    }

    @Override
    public Item getTabIconItem() {
        return ItemToolbelt.instance;
    }

    public static TetraCreativeTabs getInstance() {
        return instance;
    }

}
