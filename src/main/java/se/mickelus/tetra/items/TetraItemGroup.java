package se.mickelus.tetra.items;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import se.mickelus.tetra.blocks.geode.ItemGeode;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;

public class TetraItemGroup extends ItemGroup {

    public static TetraItemGroup instance;

    public TetraItemGroup() {
        super("tetra");

        instance = this;
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ItemGeode.instance);
    }
}
