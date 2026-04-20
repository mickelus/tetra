package se.mickelus.tetra.blocks.scroll;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.TetraRegistries;
import se.mickelus.mutil.util.HexCodec;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ScrollData {
    public static final MapCodec<ScrollData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(i -> i.key),
            Codec.STRING.optionalFieldOf("details").forGetter(i -> Optional.ofNullable(i.details)),
            Codec.BOOL.fieldOf("intricate").forGetter(i -> i.isIntricate),
            Codec.INT.fieldOf("material").forGetter(i -> i.material),
            HexCodec.instance.fieldOf("ribbon").forGetter(i -> i.ribbon),
            Codec.INT.listOf().optionalFieldOf("glyphs", Collections.emptyList()).forGetter(i -> i.glyphs),
            ResourceLocation.CODEC.listOf().optionalFieldOf("schematics", Collections.emptyList()).forGetter(i -> i.schematics),
            ResourceLocation.CODEC.listOf().optionalFieldOf("effects", Collections.emptyList()).forGetter(i -> i.craftingEffects)
    ).apply(instance, ScrollData::new));
    public static final Codec<ScrollData> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public final String key;
    public final String details;
    public final boolean isIntricate;
    public final int material;
    public final int ribbon;
    public final List<Integer> glyphs;
    public final List<ResourceLocation> schematics;
    public final List<ResourceLocation> craftingEffects;

    public ScrollData() {
        this("unknown", Optional.empty(), false, 0, 0xffffff, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public ScrollData(String name, Optional<String> details, boolean isIntricate, int material, int ribbon, List<Integer> glyphs, List<ResourceLocation> schematics, List<ResourceLocation> craftingEffects) {
        this.key = name;
        this.details = details.orElse(null);
        this.isIntricate = isIntricate;
        this.material = material;
        this.ribbon = ribbon;
        this.glyphs = List.copyOf(glyphs);
        this.schematics = List.copyOf(schematics);
        this.craftingEffects = List.copyOf(craftingEffects);
    }

    public static int readMaterialFast(ItemStack itemStack) {
        return readOptional(itemStack)
                .map(data -> data.material)
                .orElse(0);
    }

    public static int readRibbonFast(ItemStack itemStack) {
        return readOptional(itemStack)
                .map(data -> data.ribbon)
                .orElse(0);
    }

    public static ScrollData read(ItemStack itemStack) {
        return readOptional(itemStack)
                .orElseGet(ScrollData::new);
    }

    public static Optional<ScrollData> readOptional(ItemStack itemStack) {
        if (TetraRegistries.scrollData == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemStack.get(TetraRegistries.scrollData.get()));
    }

    public static ScrollData[] read(CompoundTag tag) {
        return tag.getList("data", Tag.TAG_COMPOUND).stream()
                .map(nbt -> ScrollData.CODEC.decode(NbtOps.INSTANCE, nbt))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Pair::getFirst)
                .toArray(ScrollData[]::new);
    }

    public static CompoundTag write(ScrollData[] data, CompoundTag tag) {
        ListTag list = Arrays.stream(data)
                .map(scroll -> ScrollData.CODEC.encodeStart(NbtOps.INSTANCE, scroll))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(ListTag::new));
        tag.put("data", list);
        return tag;
    }

    public static ScrollData read(JsonObject json) {
        return Optional.of(ScrollData.CODEC.decode(JsonOps.INSTANCE, json))
                .flatMap(DataResult::result)
                .map(Pair::getFirst)
                .orElse(null);
    }

    public void write(ItemStack itemStack) {
        itemStack.remove(DataComponents.CUSTOM_DATA);
        itemStack.remove(DataComponents.BLOCK_ENTITY_DATA);
        itemStack.set(TetraRegistries.scrollData.get(), this);
    }

    public JsonElement write(JsonObject json) {
        return Optional.of(ScrollData.CODEC.encode(this, JsonOps.INSTANCE, json))
                .flatMap(DataResult::result)
                .orElse(null);
    }

    public ItemStack createItemStack() {
        ItemStack itemStack = new ItemStack(ScrollItem.instance);
        write(itemStack);
        return itemStack;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ScrollData data)) {
            return false;
        }
        return isIntricate == data.isIntricate
                && material == data.material
                && ribbon == data.ribbon
                && Objects.equals(key, data.key)
                && Objects.equals(details, data.details)
                && Objects.equals(glyphs, data.glyphs)
                && Objects.equals(schematics, data.schematics)
                && Objects.equals(craftingEffects, data.craftingEffects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, details, isIntricate, material, ribbon, glyphs, schematics, craftingEffects);
    }
}
