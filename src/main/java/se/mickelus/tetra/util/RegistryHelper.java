package se.mickelus.tetra.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class RegistryHelper {
    private RegistryHelper() {
    }

    @Nullable
    public static <T> T get(Registry<T> registry, ResourceLocation location) {
        return registry.containsKey(location) ? registry.get(location) : null;
    }

    @Nullable
    public static <T> T get(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation location) {
        return lookup(registryKey).get(ResourceKey.create(registryKey, location))
                .map(Holder.Reference::value)
                .orElse(null);
    }

    @Nullable
    public static <T> ResourceLocation key(Registry<T> registry, T value) {
        return registry.getKey(value);
    }

    public static <T> Optional<ResourceLocation> key(ResourceKey<? extends Registry<T>> registryKey, T value) {
        return lookup(registryKey).listElements()
                .filter(holder -> holder.value() == value || holder.value().equals(value))
                .findFirst()
                .map(holder -> holder.key().location());
    }

    public static <T> Collection<T> values(Registry<T> registry) {
        return registry.stream().toList();
    }

    public static <T> Collection<T> values(ResourceKey<? extends Registry<T>> registryKey) {
        return lookup(registryKey).listElements()
                .map(Holder.Reference::value)
                .toList();
    }

    public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation location) {
        return TagKey.create(registryKey, location);
    }

    public static <T> Stream<T> streamTag(Registry<T> registry, TagKey<T> key) {
        return registry.getTag(key)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value);
    }

    public static <T> Stream<T> streamTag(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> key) {
        return lookup(registryKey).get(key)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value);
    }

    public static <T> boolean tagContains(Registry<T> registry, TagKey<T> key, T value) {
        return streamTag(registry, key).anyMatch(candidate -> candidate == value || candidate.equals(value));
    }

    public static <T> boolean tagContains(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> key, T value) {
        return streamTag(registryKey, key).anyMatch(candidate -> candidate == value || candidate.equals(value));
    }

    private static <T> HolderLookup.RegistryLookup<T> lookup(ResourceKey<? extends Registry<T>> registryKey) {
        return Objects.requireNonNull(CommonHooks.resolveLookup(registryKey), "Registry lookup unavailable: " + registryKey.location());
    }
}
