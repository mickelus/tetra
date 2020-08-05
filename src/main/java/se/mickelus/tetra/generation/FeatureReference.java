package se.mickelus.tetra.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class FeatureReference implements IFeatureConfig {
    public ResourceLocation location;

    public static final Codec<FeatureReference> codec = RecordCodecBuilder
            .create((instance) -> instance.group(
                    ResourceLocation.RESOURCE_LOCATION_CODEC.fieldOf("location").forGetter(i -> i.location)
            ).apply(instance, FeatureReference::new));

    public FeatureReference(ResourceLocation location) {
        this.location = location;
    }
}
