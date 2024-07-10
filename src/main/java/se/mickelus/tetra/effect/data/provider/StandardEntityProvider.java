package se.mickelus.tetra.effect.data.provider;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class StandardEntityProvider implements EntityProvider {
    private Target entity;

    public StandardEntityProvider(Target entity) {
        this.entity = entity;
    }

    @Override
    public Entity getEntity(ItemEffectContext context) {
        return switch (entity) {
            case source -> context.getUsingEntity();
            case target -> context.getTargetEntity();
        };
    }

    public enum Target {
        source,
        target
    }

    public static EntityProvider deserialize(JsonObject jsonObject) {
        return new StandardEntityProvider(Target.valueOf(jsonObject.get("entity").getAsString()));
    }
}
