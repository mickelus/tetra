package se.mickelus.tetra.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.Locale;

@MethodsReturnNonnullByDefault
public record PlainParticleOption(Vector3f color, float gravity, float friction) implements ParticleOptions {
    public static final Codec<PlainParticleOption> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(option -> option.color),
            Codec.FLOAT.fieldOf("gravity").forGetter(i -> i.gravity),
            Codec.FLOAT.fieldOf("friction").forGetter(i -> i.friction)
    ).apply(builder, PlainParticleOption::new));

    public static final Deserializer<PlainParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public PlainParticleOption fromCommand(ParticleType<PlainParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float red = reader.readFloat();
            reader.expect(' ');
            float green = reader.readFloat();
            reader.expect(' ');
            float blue = reader.readFloat();
            reader.expect(' ');
            float gravity = reader.readFloat();
            reader.expect(' ');
            float friction = reader.readFloat();
            return new PlainParticleOption(new Vector3f(red, green, blue), gravity, friction);
        }

        @Override
        public PlainParticleOption fromNetwork(ParticleType<PlainParticleOption> type, FriendlyByteBuf buffer) {
            return new PlainParticleOption(new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()), buffer.readFloat(), buffer.readFloat());
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.color.x());
        buffer.writeFloat(this.color.y());
        buffer.writeFloat(this.color.z());
        buffer.writeFloat(this.gravity);
        buffer.writeFloat(this.friction);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f", ForgeRegistries.PARTICLE_TYPES.getKey(getType()), this.color.x(), this.color.y(), this.color.z(), this.gravity, this.friction);
    }

    @Override
    public ParticleType<PlainParticleOption> getType() {
        return PlainParticleType.instance;
    }
}
