package se.mickelus.tetra.client.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.network.AbstractPacket;

public class SpawnParticlesPacket extends AbstractPacket {
    private double x;
    private double y;
    private double z;
    private float dx;
    private float dy;
    private float dz;
    private boolean randomizeVelocity;
    private int count;
    private ParticleOptions particle;

    public SpawnParticlesPacket(double x, double y, double z, float dx, float dy, float dz, int count, ParticleOptions particle) {
        this(x, y, z, dx, dy, dz, true, count, particle);
    }

    public SpawnParticlesPacket(double x, double y, double z, float dx, float dy, float dz, boolean randomizeVelocity, int count,
            ParticleOptions particle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.randomizeVelocity = randomizeVelocity;
        this.count = count;
        this.particle = particle;
    }

    public SpawnParticlesPacket() {
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(dx);
        buffer.writeFloat(dy);
        buffer.writeFloat(dz);
        buffer.writeBoolean(randomizeVelocity);
        buffer.writeInt(count);
        net.minecraft.core.particles.ParticleTypes.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, particle);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        dx = buffer.readFloat();
        dy = buffer.readFloat();
        dz = buffer.readFloat();
        randomizeVelocity = buffer.readBoolean();
        count = buffer.readInt();
        particle = net.minecraft.core.particles.ParticleTypes.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
    }

    @Override
    public void handle(Player player) {
        if (randomizeVelocity) {
            for (int i = 0; i < count; ++i) {
                player.level().addParticle(particle, x, y, z, Math.random() * dx - 0.5 * dx, Math.random() * dy - 0.5 * dy, Math.random() * dz - 0.5 * dz);
            }
        } else {
            for (int i = 0; i < count; ++i) {
                player.level().addParticle(particle, x, y, z, dx, dy, dz);
            }
        }
    }
}
