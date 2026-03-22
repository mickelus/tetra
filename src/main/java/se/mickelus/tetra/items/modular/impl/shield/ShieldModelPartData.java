package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

import java.util.List;

public class ShieldModelPartData {
    public static final Codec<UVPair> uvCodec = Codec.FLOAT.listOf().comapFlatMap(list ->
            Util.fixedSize(list, 2).map(l -> new UVPair(l.get(0), l.get(1))), (pair) -> ImmutableList.of(pair.u(), pair.v()));

    private static final Codec<Part> partCodec = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.VECTOR3F.optionalFieldOf("origin", new Vector3f()).forGetter(i -> i.origin),
            ExtraCodecs.VECTOR3F.optionalFieldOf("dimensions", new Vector3f()).forGetter(i -> i.dimensions),
            ExtraCodecs.VECTOR3F.optionalFieldOf("rotation", new Vector3f()).forGetter(i -> i.rotation),
            uvCodec.optionalFieldOf("uv", new UVPair(0, 0)).forGetter(i -> i.uv)
    ).apply(instance, Part::new));

    public static final Codec<ShieldModelPartData> codec = RecordCodecBuilder.create(instance -> instance.group(
                    partCodec.listOf().fieldOf("parts").forGetter(i -> i.parts))
            .apply(instance, ShieldModelPartData::new));

    List<Part> parts;

    public ShieldModelPartData(List<Part> parts) {
        this.parts = parts;
    }

    public void populatePartDefinition(PartDefinition partDefinition) {
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            partDefinition.addOrReplaceChild(i + "", CubeListBuilder.create()
                            .texOffs((int) part.uv.u(), (int) part.uv.v())
                            .addBox(part.origin.x(), part.origin.y(), part.origin.z(), part.dimensions.x(), part.dimensions.y(), part.dimensions.z()),
                    PartPose.rotation((float) Math.toRadians(part.rotation.x()), (float) Math.toRadians(part.rotation.y()), (float) Math.toRadians(part.rotation.z())));
        }

    }

    static class Part {
        Vector3f origin;
        Vector3f dimensions;
        Vector3f rotation;
        UVPair uv;

        public Part(Vector3f origin, Vector3f dimensions, Vector3f rotation, UVPair uv) {
            this.origin = origin;
            this.dimensions = dimensions;
            this.rotation = rotation;
            this.uv = uv;
        }
    }
}
