package se.mickelus.tetra.blocks.scroll;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
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

public class ScrollData {
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
            Codec.INT.listOf().optionalFieldOf("glyphs", Collections.emptyList()).forGetter(i -> i.glyphs),
            ResourceLocation.CODEC.listOf().optionalFieldOf("schematics", Collections.emptyList()).forGetter(i -> i.schematics),
            ResourceLocation.CODEC.listOf().optionalFieldOf("effects", Collections.emptyList()).forGetter(i -> i.craftingEffects)
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

    public static ScrollData read(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getChildTag("BlockEntityTag"))
                .map(ScrollData::read)
                .filter(data -> data.length > 0)
                .map(data -> data[0])
                .orElseGet(ScrollData::new);
    }

    public void write(ItemStack itemStack) {
        itemStack.setTagInfo("BlockEntityTag", ScrollData.write(new ScrollData[] { this }, new CompoundNBT()));
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

    public static ScrollData read(JsonObject json) {
        return Optional.of(ScrollData.codec.decode(JsonOps.INSTANCE, json))
                .flatMap(DataResult::result)
                .map(Pair::getFirst)
                .orElse(null);
    }

    public JsonElement write(JsonObject json) {
        return Optional.of(ScrollData.codec.encode(this, JsonOps.INSTANCE, json))
                .flatMap(DataResult::result)
                .orElse(null);
    }
}
