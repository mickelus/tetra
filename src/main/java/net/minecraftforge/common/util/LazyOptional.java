package net.minecraftforge.common.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);

    private Supplier<? extends T> supplier;
    private boolean resolved;
    private T value;

    private LazyOptional(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyOptional<T> empty() {
        @SuppressWarnings("unchecked")
        LazyOptional<T> empty = (LazyOptional<T>) EMPTY;
        return empty;
    }

    public static <T> LazyOptional<T> of(Supplier<? extends T> supplier) {
        return new LazyOptional<>(Objects.requireNonNull(supplier));
    }

    public boolean isPresent() {
        return resolve().isPresent();
    }

    public void ifPresent(Consumer<? super T> consumer) {
        resolve().ifPresent(consumer);
    }

    public T orElse(T other) {
        return resolve().orElse(other);
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return resolve().orElseGet(supplier);
    }

    public <R> LazyOptional<R> map(Function<? super T, ? extends R> mapper) {
        return lazyMap(mapper);
    }

    public <R> LazyOptional<R> lazyMap(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return isPresent()
                ? LazyOptional.of(() -> mapper.apply(orElse(null)))
                : LazyOptional.empty();
    }

    public <R> LazyOptional<R> cast() {
        return lazyMap(value -> {
            @SuppressWarnings("unchecked")
            R casted = (R) value;
            return casted;
        });
    }

    public Optional<T> resolve() {
        if (!resolved) {
            resolved = true;
            value = supplier != null ? supplier.get() : null;
            supplier = null;
        }
        return Optional.ofNullable(value);
    }

    public void invalidate() {
        resolved = true;
        value = null;
        supplier = null;
    }
}
