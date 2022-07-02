package se.mickelus.tetra.blocks.geode.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class SparkleParticleType {
    public static final String identifier = "sparkle";

    @ObjectHolder(registryName = "particle_type", value = TetraMod.MOD_ID + ":" + identifier)
    public static SimpleParticleType instance;
}
