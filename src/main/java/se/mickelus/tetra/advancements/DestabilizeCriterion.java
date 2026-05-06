package se.mickelus.tetra.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DestabilizeCriterion {
    public static final GenericTrigger<Instance> trigger = new GenericTrigger<>(Instance.CODEC);

    public static void trigger(ServerPlayer player, ItemStack usedItem) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(usedItem));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item)
        ).apply(instance, Instance::new));

        public boolean test(ItemStack usedItem) {
            return item.isEmpty() || item.get().test(usedItem);
        }
    }
}
