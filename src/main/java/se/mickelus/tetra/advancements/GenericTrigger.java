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

    public void fulfillCriterion(ServerPlayer player, Predicate<T> validationPredicate) {
        trigger(player, validationPredicate);
    }
}
