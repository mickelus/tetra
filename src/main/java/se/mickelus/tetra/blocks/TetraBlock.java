package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.items.TetraItemGroup;

public class TetraBlock extends Block implements ITetraBlock {

    protected boolean hasItem = false;

    public TetraBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasItem() {
        return hasItem;
    }
}
