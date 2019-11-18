package se.mickelus.tetra.items;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.geode.GeodeItem;

public class TetraItemGroup extends ItemGroup {

    public static TetraItemGroup instance;

    public TetraItemGroup() {
        super("tetra");

        instance = this;
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(GeodeItem.instance);
    }
}
