package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class BlockItemEffectCondition extends ItemEffectCondition {
    PropertyMatcher block;
    VectorProvider position;

    @Override
    public boolean test(ItemEffectContext context) {
        return block.test(context.getLevel().getBlockState(position.getBlockPos(context)));
    }
}
