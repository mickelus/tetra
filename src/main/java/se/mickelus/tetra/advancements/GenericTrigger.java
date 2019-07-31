package se.mickelus.tetra.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericTrigger<T extends ICriterionInstance> implements ICriterionTrigger<T> {
    private ResourceLocation id;
    private Map<PlayerAdvancements, Set<Listener<T>>> listeners;
    private Function<JsonObject, T> deserializer;

    public GenericTrigger(String id, Function<JsonObject, T> deserializer) {
        this.id = new ResourceLocation(id);
        listeners = Maps.newHashMap();
        this.deserializer = deserializer;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(PlayerAdvancements advancements, Listener<T> listener) {
        listeners.computeIfAbsent(advancements, key -> Sets.newHashSet())
                .add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements advancements, Listener<T> listener) {
        Optional.ofNullable(listeners.get(advancements))
                .ifPresent(advancementListeners -> {
                    advancementListeners.remove(listener);

                    if (advancementListeners.isEmpty()) {
                        this.listeners.remove(advancements);
                    }
                });
    }

    @Override
    public void removeAllListeners(PlayerAdvancements advancements) {
        this.listeners.remove(advancements);
    }

    @Override
    public T deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return deserializer.apply(json);
    }

    /**
     * Fulfills all criterion instances that pass the validation predicate.
     * @param advancements An advancements object from the player that the criterion is to be fulfilled for
     * @param validationPredicate A predicate used to check which criterion will be fulfilled
     */
    public void fulfillCriterion(PlayerAdvancements advancements, Predicate<T> validationPredicate) {
        Collection<Listener<T>> advancementListeners = Optional.ofNullable(listeners.get(advancements))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(listener -> validationPredicate.test(listener.getCriterionInstance()))
                .collect(Collectors.toList());

        // grantCriterion removes the listeners from the list, so we run this separately to avoid concurrent modification issues
        for (Listener<T> listener: advancementListeners) {
            listener.grantCriterion(advancements);
        }
    }
}
