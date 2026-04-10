package se.mickelus.tetra.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.blocks.PropertyMatcher;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class BlockUseCriterion {
    public static final GenericTrigger<Instance> trigger = new GenericTrigger<>(Instance.CODEC);

    public static void trigger(ServerPlayer player, BlockState state, ItemStack usedItem, Map<String, String> data) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(state, usedItem, data));
    }

    public static void trigger(ServerPlayer player, BlockState state, ItemStack usedItem) {
        trigger(player, state, usedItem, Collections.emptyMap());
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<PropertyMatcher> before,
            Optional<PropertyMatcher> after,
            Optional<ItemPredicate> item,
            Map<String, String> data
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                PropertyMatcher.CODEC.optionalFieldOf("before").forGetter(Instance::before),
                PropertyMatcher.CODEC.optionalFieldOf("after").forGetter(Instance::after),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("data", Map.of()).forGetter(Instance::data)
        ).apply(instance, Instance::new));

        public boolean test(BlockState state, ItemStack usedItem, Map<String, String> data) {
            if (before.isPresent() && !before.get().test(state)) {
                return false;
            }

            if (after.isPresent() && !after.get().test(state)) {
                return false;
            }

            if (item.isPresent() && !item.get().test(usedItem)) {
                return false;
            }

            return this.data.entrySet().stream()
                    .noneMatch(entry -> !data.containsKey(entry.getKey()) || !entry.getValue().equals(data.get(entry.getKey())));
        }
    }
}
