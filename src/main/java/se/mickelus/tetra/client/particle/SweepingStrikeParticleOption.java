package se.mickelus.tetra.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

@MethodsReturnNonnullByDefault
public record SweepingStrikeParticleOption(int duration, boolean reverse, float pitch, float yaw) implements ParticleOptions {
    public static final Codec<SweepingStrikeParticleOption> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("duration").forGetter(option -> option.duration),
            Codec.BOOL.fieldOf("reverse").forGetter(option -> option.reverse),
            Codec.FLOAT.fieldOf("pitch").forGetter(option -> option.pitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(option -> option.yaw)
    ).apply(builder, SweepingStrikeParticleOption::new));

    public static final Deserializer<SweepingStrikeParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public SweepingStrikeParticleOption fromCommand(ParticleType<SweepingStrikeParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int duration = reader.readInt();
            reader.expect(' ');
            boolean reverse = reader.readBoolean();
            reader.expect(' ');
            float pitch = reader.readFloat();
            reader.expect(' ');
            float yaw = reader.readFloat();
            return new SweepingStrikeParticleOption(duration, reverse, pitch, yaw);
        }

        @Override
        public SweepingStrikeParticleOption fromNetwork(ParticleType<SweepingStrikeParticleOption> type, FriendlyByteBuf buffer) {
            return new SweepingStrikeParticleOption(buffer.readVarInt(), buffer.readBoolean(), buffer.readFloat(), buffer.readFloat());
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(duration);
        buffer.writeBoolean(reverse);
        buffer.writeFloat(pitch);
        buffer.writeFloat(yaw);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %b %.2f %.2f", Registry.PARTICLE_TYPE.getKey(getType()), duration, reverse, pitch, yaw);
    }

    @Override
    public ParticleType<SweepingStrikeParticleOption> getType() {
        return SweepingStrikeParticleType.instance;
    }
}
