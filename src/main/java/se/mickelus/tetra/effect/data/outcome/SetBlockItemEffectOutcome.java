package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class SetBlockItemEffectOutcome extends ItemEffectOutcome {
    BlockState block;
    VectorProvider position;

    @Override
    public boolean perform(ItemEffectContext context) {
        return context.getLevel().setBlock(position.getBlockPos(context), block, Block.UPDATE_ALL);
    }
}
