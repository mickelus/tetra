package se.mickelus.tetra.util;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class CastOptional {
    public static <T> Optional<T> cast(@Nullable Object object, Class<T> clazz) {
        return Optional.ofNullable(object)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }
}
