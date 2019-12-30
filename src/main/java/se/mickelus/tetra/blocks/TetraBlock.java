package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;

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
