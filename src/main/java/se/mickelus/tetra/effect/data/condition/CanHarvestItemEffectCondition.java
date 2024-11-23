package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class CanHarvestItemEffectCondition extends ItemEffectCondition {
    VectorProvider position;

    @Override
    public boolean test(ItemEffectContext context) {
        return context.getUsedItemStack().isCorrectToolForDrops(context.getLevel().getBlockState(position.getBlockPos(context)));
    }
}
