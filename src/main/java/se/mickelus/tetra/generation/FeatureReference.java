package se.mickelus.tetra.generation;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class FeatureReference implements IFeatureConfig {
    public ResourceLocation location;

    public FeatureReference(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(ops.createString("location"), ops.createString(location.toString()))));
    }

    public static <T> FeatureReference deserialize(Dynamic<T> data) {
        return new FeatureReference(new ResourceLocation(data.get("location").asString("")));
    }
}
