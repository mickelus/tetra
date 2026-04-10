package se.mickelus.tetra.compat.forge.forgespi;

import net.neoforged.fml.loading.FMLEnvironment;

public final class Environment {
    private static final Environment INSTANCE = new Environment();

    public static Environment get() {
        return INSTANCE;
    }

    public net.neoforged.api.distmarker.Dist getDist() {
        return FMLEnvironment.dist;
    }
}
