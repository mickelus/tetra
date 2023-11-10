package se.mickelus.tetra.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamHelper {
    public static <T> Collector<T, ?, List<T>> toShuffledList() {
        return toShuffledList(new Random());
    }

    public static <T> Collector<T, ?, List<T>> toShuffledList(Random random) {
        return Collectors.collectingAndThen(
                Collectors.toCollection(ArrayList::new),
                list -> {
                    Collections.shuffle(list, random);
                    return list;
                });
    }
}
