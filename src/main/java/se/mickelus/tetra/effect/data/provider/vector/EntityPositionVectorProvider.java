package se.mickelus.tetra.effect.data.provider.vector;

import com.google.gson.JsonObject;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;

public class EntityPositionVectorProvider implements VectorProvider {
    private EntityProvider entity;
    private Origin origin = Origin.feet;

    public EntityPositionVectorProvider(EntityProvider entity) {
        this.entity = entity;
    }

    public EntityPositionVectorProvider(EntityProvider entity, Origin origin) {
        this(entity);
        this.origin = origin;
    }

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        return switch (origin) {
            case feet -> entity.getEntity(context).position();
            case head -> entity.getEntity(context).getEyePosition();
            case center -> entity.getEntity(context).position().add(0, entity.getEntity(context).getEyeHeight() / 2, 0);
        };
    }

    public enum Origin {
        feet,
        head,
        center
    }

    public static VectorProvider deserialize(JsonObject jsonObject) {
        return DataManager.gson.fromJson(jsonObject, EntityPositionVectorProvider.class);
    }
}
