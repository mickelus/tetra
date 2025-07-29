package se.mickelus.tetra.effect.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.outcome.ItemEffectOutcome;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemEffectData {
    public ItemEffect effect;
    Data data;
    public ItemEffectTrigger trigger;
    public ItemEffectCondition condition;
    public ItemEffectOutcome outcome;

    public static Map<String, Float> calculateNumbers(ItemEffectData.Data dataProviders, ItemEffectContext context) {
        if (dataProviders != null && dataProviders.numbers != null) {
            return calculateNumbers(dataProviders.numbers, context);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Float> calculateNumbers(Map<String, NumberProvider> data, ItemEffectContext context) {
        Map<String, Float> result = new HashMap<>();
        ItemEffectContext updatedContext = context;
        for (Map.Entry<String, NumberProvider> entry : data.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue(updatedContext));
            updatedContext = updatedContext.withNumbers(result);
        }
        return result;
    }

    public static Map<String, Vec3> calculateVectors(ItemEffectData.Data dataProviders, ItemEffectContext context) {
        if (dataProviders != null && dataProviders.vectors != null) {
            return calculateVectors(dataProviders.vectors, context);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Vec3> calculateVectors(Map<String, VectorProvider> data, ItemEffectContext context) {
        Map<String, Vec3> result = new HashMap<>();
        ItemEffectContext updatedContext = context;
        for (Map.Entry<String, VectorProvider> entry : data.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getVector(updatedContext));
            updatedContext = updatedContext.withVectors(result);
        }
        return result;
    }

    public static Map<String, Entity> calculateEntities(ItemEffectData.Data dataProviders, ItemEffectContext context) {
        if (dataProviders != null && dataProviders.entities != null) {
            return calculateEntities(dataProviders.entities, context);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Entity> calculateEntities(Map<String, EntityProvider> data, ItemEffectContext context) {
        Map<String, Entity> result = new HashMap<>();
        ItemEffectContext updatedContext = context;
        for (Map.Entry<String, EntityProvider> entry : data.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getEntity(updatedContext));
            updatedContext = updatedContext.withEntities(result);
        }
        return result;
    }

    public record Data(Map<String, NumberProvider> numbers, Map<String, VectorProvider> vectors, Map<String, EntityProvider> entities) {
    }
}
