package se.mickelus.tetra.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class GenericTrigger<T extends SimpleCriterionTrigger.SimpleInstance> extends SimpleCriterionTrigger<T> {
    private final Codec<T> codec;

    public GenericTrigger(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    /**
     * Fulfills all criterion instances that pass the validation predicate.
     *
     * @param player              The player that the criterion is to be fulfilled for
     * @param validationPredicate A predicate used to check which criterion will be fulfilled
     */
    public void fulfillCriterion(ServerPlayer player, Predicate<T> validationPredicate) {
        trigger(player, validationPredicate);
    }
}
