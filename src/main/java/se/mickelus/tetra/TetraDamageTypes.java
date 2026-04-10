package se.mickelus.tetra;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public interface TetraDamageTypes {
    ResourceKey<DamageType> bleeding = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("tetra:bleeding"));
}
