package se.mickelus.tetra.blocks.scroll;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import se.mickelus.tetra.util.HexCodec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ScrollData {
    public String key;
    public boolean isIntricate;
    public int material = 0;
    public int ribbon = 0xffffff;
    public List<Integer> glyphs = Collections.emptyList();
    public List<ResourceLocation> schematics = Collections.emptyList();
    public List<ResourceLocation> craftingEffects = Collections.emptyList();

    private static final Codec<ScrollData> codec = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(i -> i.key),
            Codec.BOOL.fieldOf("intricate").forGetter(i -> i.isIntricate),
            Codec.INT.fieldOf("material").forGetter(i -> i.material),
            HexCodec.instance.fieldOf("ribbon").forGetter(i -> i.ribbon),
            Codec.INT.listOf().fieldOf("glyphs").forGetter(i -> i.glyphs),
            ResourceLocation.CODEC.listOf().fieldOf("schematics").forGetter(i -> i.schematics),
            ResourceLocation.CODEC.listOf().fieldOf("craftingEffects").forGetter(i -> i.craftingEffects)
    ).apply(instance, ScrollData::new));

    public ScrollData() {
        key = "unknown";
    }

    public ScrollData(String name, boolean isIntricate, int material, int ribbon, List<Integer> glyphs, List<ResourceLocation> schematics, List<ResourceLocation> craftingEffects) {
        this.key = name;
        this.isIntricate = isIntricate;

        this.material = material;
        this.ribbon = ribbon;
        this.glyphs = glyphs;

        if (!schematics.isEmpty()) {
            this.schematics = schematics;
        }

        if (!craftingEffects.isEmpty()) {
            this.craftingEffects = craftingEffects;
        }
    }

    public static CompoundNBT write(ScrollData[] data, CompoundNBT tag) {
        ListNBT list = Arrays.stream(data)
                .map(scroll -> ScrollData.codec.encodeStart(NBTDynamicOps.INSTANCE, scroll))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(ListNBT::new));
        tag.put("data", list);
        return tag;
    }

    public static ScrollData[] read(CompoundNBT tag) {
        return tag.getList("data", Constants.NBT.TAG_COMPOUND).stream()
                .map(nbt -> ScrollData.codec.decode(NBTDynamicOps.INSTANCE, nbt))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Pair::getFirst)
                .toArray(ScrollData[]::new);
    }
}
