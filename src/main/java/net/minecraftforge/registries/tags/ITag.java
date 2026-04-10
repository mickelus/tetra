package net.minecraftforge.registries.tags;

import java.util.stream.Stream;

public interface ITag<T> extends Iterable<T> {
    boolean contains(T value);

    boolean isEmpty();

    Stream<T> stream();
}
