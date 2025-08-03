package se.mickelus.tetra.effect.data.outcome;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;
import se.mickelus.tetra.util.StreamHelper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FindBlocksItemEffectOutcome extends ItemEffectOutcome {
    PropertyMatcher condition;
    ItemEffectCondition requireReplaceable;
    ItemEffectCondition solidBelow;
    ItemEffectCondition solidAdjacent;
    VectorProvider origin;
    AABB bounds;
    ItemEffectCondition random = new FixedItemEffectCondition(false);
    NumberProvider count = (context) -> Integer.MAX_VALUE;
    ItemEffectOutcome outcome;

    @Override
    public boolean perform(ItemEffectContext context) {
        Collector<BlockPos, ?, List<BlockPos>> collector = random.test(context)
            ? StreamHelper.toShuffledList()
            : Collectors.toUnmodifiableList();

        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger successCounter = new AtomicInteger(0);
        BlockPos.betweenClosedStream(bounds.move(origin.getBlockPos(context)))
            .map(BlockPos::new)
            .collect(collector)
            .stream()
            .map(pos -> Pair.of(pos, context.getLevel().getBlockState(pos)))
            .filter(pair -> condition == null || condition.test(pair.getSecond()))
            .filter(pair -> testOtherConditions(context, pair.getSecond(), pair.getFirst()))
            .limit(count.getIntegerValue(context))
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

    private boolean testOtherConditions(ItemEffectContext context, BlockState blockState, BlockPos blockPos) {
        return (requireReplaceable == null || requireReplaceable.test(context) == blockState.canBeReplaced())
            && (solidBelow == null || solidBelow.test(context) == MultifaceBlock.canAttachTo(context.getLevel(), Direction.DOWN, blockPos.below(),
            context.getLevel().getBlockState(blockPos.below())))
            && (solidAdjacent == null || solidAdjacent.test(context) == isSolidAdjacent(context.getLevel(), blockPos));
    }

    private boolean isSolidAdjacent(Level level, BlockPos blockPos) {
        return Arrays.stream(Direction.values())
            .anyMatch(dir -> MultifaceBlock.canAttachTo(level, dir, blockPos.relative(dir), level.getBlockState(blockPos.relative(dir))));
    }
}
