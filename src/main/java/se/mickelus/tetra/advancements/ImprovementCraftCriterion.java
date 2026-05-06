package se.mickelus.tetra.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ImprovementCraftCriterion {
    public static final GenericTrigger<Instance> trigger = new GenericTrigger<>(Instance.CODEC);

    public static void trigger(ServerPlayer player, ItemStack before, ItemStack after, String schematic, String slot, String improvement,
            int improvementLevel, ItemAbility toolAction, int toolLevel) {
        trigger.fulfillCriterion(player,
                criterion -> criterion.test(before, after, schematic, slot, improvement, improvementLevel, toolAction, toolLevel));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<ItemPredicate> before,
            Optional<ItemPredicate> after,
            Optional<String> schematic,
            Optional<String> slot,
            Optional<String> improvement,
            Optional<Integer> improvementLevel,
            Optional<ItemAbility> tool,
            MinMaxBounds.Ints toolLevel
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ItemPredicate.CODEC.optionalFieldOf("before").forGetter(Instance::before),
                ItemPredicate.CODEC.optionalFieldOf("after").forGetter(Instance::after),
                Codec.STRING.optionalFieldOf("schematic").forGetter(Instance::schematic),
                Codec.STRING.optionalFieldOf("slot").forGetter(Instance::slot),
                Codec.STRING.optionalFieldOf("improvement").forGetter(Instance::improvement),
                Codec.INT.optionalFieldOf("improvementLevel").forGetter(Instance::improvementLevel),
                ItemAbility.CODEC.optionalFieldOf("tool").forGetter(Instance::tool),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("toolLevel", MinMaxBounds.Ints.ANY).forGetter(Instance::toolLevel)
        ).apply(instance, Instance::new));

        public boolean test(ItemStack before, ItemStack after, String schematic, String slot, String improvement, int improvementLevel,
                ItemAbility toolAction, int toolLevel) {
            if (this.before.isPresent() && !this.before.get().test(before)) {
                return false;
            }

            if (this.after.isPresent() && !this.after.get().test(after)) {
                return false;
            }

            if (this.schematic.isPresent() && !this.schematic.get().equals(schematic)) {
                return false;
            }

            if (this.slot.isPresent() && !this.slot.get().equals(slot)) {
                return false;
            }

            if (this.improvement.isPresent() && !this.improvement.get().equals(improvement)) {
                return false;
            }

            if (this.improvementLevel.isPresent() && this.improvementLevel.get() != improvementLevel) {
                return false;
            }

            if (this.tool.isPresent() && !this.tool.get().equals(toolAction)) {
                return false;
            }

            return this.toolLevel.matches(toolLevel);
        }
    }
}
