package se.mickelus.tetra.effect.data.provider.vector;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.ItemEffectData;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

import java.util.Map;

public class ExpressionVectorProvider implements VectorProvider {
    Map<String, VectorProvider> vectors;
    Map<String, NumberProvider> numbers;
    VectorProvider result;

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        ItemEffectContext updatedContext = context;

        if (vectors != null) {
            updatedContext = updatedContext.withMergedVectors(ItemEffectData.calculateVectors(vectors, updatedContext));
        }

        for (Map.Entry<String, Vec3> entry : updatedContext.getVectors().entrySet()) {
            String key = entry.getKey();
            Vec3 pos = entry.getValue();
            updatedContext = updatedContext.withMergedNumbers(ImmutableMap.of(
                    key + "X", (float) pos.x,
                    key + "Y", (float) pos.y,
                    key + "Z", (float) pos.z
            ));
        }
        
        if (numbers != null) {
            updatedContext = updatedContext.withMergedNumbers(ItemEffectData.calculateNumbers(numbers, updatedContext));
        }

        return result.getVector(updatedContext);
    }
}
