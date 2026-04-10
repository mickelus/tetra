package se.mickelus.tetra.compat.forge.common.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class NonNullLazy<T> implements Supplier<T> {
    private Supplier<T> supplier;
    private volatile T value;

    private NonNullLazy(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public static <T> NonNullLazy<T> of(Supplier<T> supplier) {
        return new NonNullLazy<>(supplier);
    }

    @Override
    public T get() {
        T result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    result = Objects.requireNonNull(supplier.get());
                    value = result;
                    supplier = null;
                }
            }
        }
        return result;
    }
}
