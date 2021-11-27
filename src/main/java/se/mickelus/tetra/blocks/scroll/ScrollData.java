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
    public String details;
    public boolean isIntricate;
    public int material = 0;
    public int ribbon = 0xffffff;
    public List<Integer> glyphs = Collections.emptyList();
    public List<ResourceLocation> schematics = Collections.emptyList();
    public List<ResourceLocation> craftingEffects = Collections.emptyList();

    private static final Codec<ScrollData> codec = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(i -> i.key),
            Codec.STRING.optionalFieldOf("details").forGetter(i -> Optional.ofNullable(i.details)),
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

    public ScrollData(String name, Optional<String> details, boolean isIntricate, int material, int ribbon, List<Integer> glyphs, List<ResourceLocation> schematics, List<ResourceLocation> craftingEffects) {
        this.key = name;
        this.details = details.orElse(null);
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

    public static int readMaterialFast(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getChildTag("BlockEntityTag"))
                .map(tag -> tag.getList("data", Constants.NBT.TAG_COMPOUND))
                .filter(list -> list.size() > 0)
                .map(list -> list.getCompound(0))
                .map(tag -> tag.getInt("material"))
                .orElse(0);
    }

    public static int readRibbonFast(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getChildTag("BlockEntityTag"))
                .map(tag -> tag.getList("data", Constants.NBT.TAG_COMPOUND))
                .filter(list -> list.size() > 0)
                .map(list -> list.getCompound(0))
                .map(tag -> tag.getString("ribbon"))
                .map(hex -> (int) Long.parseLong(hex, 16))
                .orElse(0);
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
