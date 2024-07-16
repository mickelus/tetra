package se.mickelus.tetra.effect.data.outcome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

import java.util.concurrent.atomic.AtomicBoolean;

public class FindBlocksItemEffectOutcome extends ItemEffectOutcome {
    PropertyMatcher predicate;
    VectorProvider origin;
    AABB bounds;
    ItemEffectOutcome outcome;

    @Override
    public boolean perform(ItemEffectContext context) {
        AtomicBoolean result = new AtomicBoolean(false);
        BlockPos.betweenClosedStream(bounds.move(origin.getBlockPos(context)))
                .map(pos -> Pair.of(pos, context.getLevel().getBlockState(pos)))
                .filter(pair -> predicate.test(pair.getSecond()))
                .forEach(state -> {
                    ItemEffectContext updatedContext = context.withBlock(state.getFirst(), context.getTargetState());
                    boolean success = outcome.perform(context);
                    if (success) {
                        result.set(true);
                    }
                });
        return result.get();
    }
}
