package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.data.BlockStateVariantBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericTrigger<T extends CriterionInstance> extends AbstractCriterionTrigger<T> {
    private ResourceLocation id;

    private TriggerDeserializer<T> deserializer;

    public GenericTrigger(String id, TriggerDeserializer<T> deserializer) {
        super();
        this.id = new ResourceLocation(id);
        this.deserializer = deserializer;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected T deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return deserializer.apply(json, entityPredicate, conditionsParser);
    }

    /**
     * Fulfills all criterion instances that pass the validation predicate.
     * @param player The player that the criterion is to be fulfilled for
     * @param validationPredicate A predicate used to check which criterion will be fulfilled
     */
    public void fulfillCriterion(ServerPlayerEntity player, Predicate<T> validationPredicate) {
        triggerListeners(player, validationPredicate);
    }

    public static interface TriggerDeserializer<T> {
        T apply(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser);
    }
}
