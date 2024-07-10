package se.mickelus.tetra.effect.data.provider;

import com.google.gson.JsonObject;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class EntityPositionProvider implements PositionProvider {
    private EntityProvider entity;
    private Origin origin = Origin.feet;

    public EntityPositionProvider(EntityProvider entity) {
        this.entity = entity;
    }

    public EntityPositionProvider(EntityProvider entity, Origin origin) {
        this(entity);
        this.origin = origin;
    }

    @Override
    public Vec3 getPosition(ItemEffectContext context) {
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

    public static PositionProvider deserialize(JsonObject jsonObject) {
        return DataManager.gson.fromJson(jsonObject, EntityPositionProvider.class);
    }
}
