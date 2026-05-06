package se.mickelus.tetra.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.tetra.blocks.PropertyMatcher;

import java.util.Optional;

public class BlockInteractionCriterion {
    public static final GenericTrigger<Instance> trigger = new GenericTrigger<>(Instance.CODEC);

    public static void trigger(ServerPlayer player, BlockState beforeState, BlockState afterState, ItemAbility usedItemAbility, int usedToolLevel) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(beforeState, afterState, usedItemAbility, usedToolLevel));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<PropertyMatcher> before,
            Optional<PropertyMatcher> after,
            Optional<ItemAbility> tool,
            Optional<Integer> toolLevel
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                PropertyMatcher.CODEC.optionalFieldOf("before").forGetter(Instance::before),
                PropertyMatcher.CODEC.optionalFieldOf("after").forGetter(Instance::after),
                ItemAbility.CODEC.optionalFieldOf("tool").forGetter(Instance::tool),
                Codec.INT.optionalFieldOf("toolLevel").forGetter(Instance::toolLevel)
        ).apply(instance, Instance::new));

        public boolean test(BlockState beforeState, BlockState afterState, ItemAbility usedItemAbility, int usedToolLevel) {
            if (before.isPresent() && !before.get().test(beforeState)) {
                return false;
            }

            if (after.isPresent() && !after.get().test(afterState)) {
                return false;
            }

            if (this.tool.isPresent() && !this.tool.get().equals(usedItemAbility)) {
                return false;
            }

            return this.toolLevel.isEmpty() || this.toolLevel.get() == usedToolLevel;
        }
    }
}
