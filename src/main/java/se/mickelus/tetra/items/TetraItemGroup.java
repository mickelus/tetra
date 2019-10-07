package se.mickelus.tetra.items;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;

public class TetraItemGroup extends ItemGroup {

    private static TetraItemGroup instance;

    public TetraItemGroup() {
        super("tetra");

        instance = this;
    }

    @Override
    public ItemStack createIcon() {
        return ItemDuplexToolModular.instance.createHammerStack("log", "stick");
    }

    public static TetraItemGroup getInstance() {
        return instance;
    }
}
