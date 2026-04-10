package se.mickelus.tetra.client.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;
import se.mickelus.tetra.TetraMod;

public class Particles {
    public static RegistryObject<SimpleParticleType> arcaneFire;
    public static RegistryObject<SimpleParticleType> sputteringPower;
    public static RegistryObject<SimpleParticleType> splinteredPower;

    public static void addSputteringPower(ServerLevel level, double x, double y, double z, Entity entity) {
        level.sendParticles(sputteringPower.get(), x, y, z, 0, entity.getId(), 0, 0, 1);
    }

    public static void addSputteringPower(ServerLevel level, double x, double y, double z, Entity entity, int count) {
        TetraMod.packetHandler.sendToAllPlayersNear(new SpawnParticlesPacket(x, y, z, entity.getId(), 0, 0, false, count, sputteringPower.get()),
                new BlockPos((int) x, (int) y, (int) z), 64, level.dimension());
    }
}
