package se.mickelus.tetra.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.blocks.geode.GeodeItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraItemGroup extends CreativeModeTab {

    public static TetraItemGroup instance;

    public TetraItemGroup() {
        super("tetra");

        instance = this;
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(GeodeItem.instance);
    }
}
