package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class DamageEntityItemEffectOutcome extends ItemEffectOutcome {
    EntityProvider entity;
    NumberProvider amount;
    ResourceLocation damageType;

    @Override
    public boolean perform(ItemEffectContext context) {
        Entity targetEntity = entity.getEntity(context);
        if (targetEntity != null) {
            return targetEntity.hurt(targetEntity.damageSources().source(ResourceKey.create(Registries.DAMAGE_TYPE, damageType)), amount.getValue(context));
        }
        return false;
    }
}
