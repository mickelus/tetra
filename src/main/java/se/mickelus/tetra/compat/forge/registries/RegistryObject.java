package se.mickelus.tetra.compat.forge.registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public final class RegistryObject<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final ResourceLocation id;
    private final ResourceKey<?> key;

    private RegistryObject(Supplier<T> supplier, ResourceLocation id, ResourceKey<?> key) {
        this.supplier = supplier;
        this.id = id;
        this.key = key;
    }

    public static <R, T extends R> RegistryObject<T> of(DeferredHolder<R, T> holder) {
        return new RegistryObject<>(holder::get, holder.getId(), holder.getKey());
    }

    @Override
    public T get() {
        return supplier.get();
    }

    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public ResourceKey<T> getKey() {
        return (ResourceKey<T>) key;
    }

    public boolean isPresent() {
        try {
            return get() != null;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
