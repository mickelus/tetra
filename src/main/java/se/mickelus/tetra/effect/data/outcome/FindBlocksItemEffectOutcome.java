package se.mickelus.tetra.effect.data.outcome;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

import java.util.concurrent.atomic.AtomicInteger;

public class FindBlocksItemEffectOutcome extends ItemEffectOutcome {
    PropertyMatcher predicate;
    VectorProvider origin;
    AABB bounds;
    ItemEffectOutcome outcome;

    @Override
    public boolean perform(ItemEffectContext context) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger successCounter = new AtomicInteger(0);
        BlockPos.betweenClosedStream(bounds.move(origin.getBlockPos(context)))
                .map(pos -> Pair.of(pos, context.getLevel().getBlockState(pos)))
                .filter(pair -> predicate.test(pair.getSecond()))
                .forEach(state -> {
                    ItemEffectContext updatedContext = context
                            .withMergedVectors(ImmutableMap.of("ref", Vec3.atLowerCornerOf(state.getFirst())))
                            .withMergedNumbers(ImmutableMap.of("index", counter.floatValue(), "successCount", successCounter.floatValue()));
                    boolean success = outcome.perform(updatedContext);
                    if (success) {
                        successCounter.incrementAndGet();
                    }
                    counter.incrementAndGet();
                });
        return successCounter.get() > 0;
    }
}
