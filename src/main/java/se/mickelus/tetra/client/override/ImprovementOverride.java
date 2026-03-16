package se.mickelus.tetra.client.override;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ImprovementOverride {
    public static ImprovementOverride names = new ImprovementOverride();
    public static ImprovementOverride descriptions = new ImprovementOverride();

    private Map<String, Resolver> overrides = new HashMap<>();

    public void register(String key, Resolver function) {
        overrides.put(key, function);
    }

    public String resolve(String key, int level, @Nullable ItemStack itemStack) {
        return Optional.ofNullable(overrides.get(key))
                .map(resolver -> resolver.resolve(key, level, itemStack))
                .orElse(null);
    }

    public boolean hasOverride(String key) {
        return overrides.containsKey(key);
    }

    @Nullable
    public interface Resolver {
        String resolve(String improvementKey, int level, @Nullable ItemStack itemStack);
    }
}
