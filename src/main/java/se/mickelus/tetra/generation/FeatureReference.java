package se.mickelus.tetra.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class FeatureReference implements FeatureConfiguration {
    public ResourceLocation location;

    public static final Codec<FeatureReference> codec = RecordCodecBuilder
            .create((instance) -> instance.group(
                    ResourceLocation.CODEC.fieldOf("location").forGetter(i -> i.location)
            ).apply(instance, FeatureReference::new));

    public FeatureReference(ResourceLocation location) {
        this.location = location;
    }
}
