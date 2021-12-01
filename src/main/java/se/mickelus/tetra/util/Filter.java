package se.mickelus.tetra.util;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
@ParametersAreNonnullByDefault
public class Filter {
    /**
     * Filter a stream so that only one element for each "key" remains, the key is determined by the value returned
     * by keyExtractor.
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinct(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
