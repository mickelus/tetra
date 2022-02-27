package se.mickelus.tetra.blocks.geode.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class SparkleParticleType {
    public static final String identifier = "sparkle";

    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static SimpleParticleType instance;
}
