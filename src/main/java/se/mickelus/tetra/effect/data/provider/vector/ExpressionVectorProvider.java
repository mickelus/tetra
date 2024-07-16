package se.mickelus.tetra.effect.data.provider.vector;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Map;

public class ExpressionVectorProvider implements VectorProvider {
    Map<String, VectorProvider> values;
    VectorProvider result;

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        ItemEffectContext updatedContext = context;
        for (Map.Entry<String, VectorProvider> entry : values.entrySet()) {
            String key = entry.getKey();
            Vec3 pos = entry.getValue().getVector(updatedContext);
            updatedContext = updatedContext.withMergedData(ImmutableMap.of(
                    key + "X", (float) pos.x,
                    key + "Y", (float) pos.y,
                    key + "Z", (float) pos.z
            ));
        }
        return result.getVector(updatedContext);
    }
}
