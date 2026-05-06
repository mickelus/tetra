package se.mickelus.tetra.data.predicate;

import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

// Cross-version compat: this interface matches upstream 1.20's predicate shape. Do not fold
// onto vanilla ItemSubPredicate — rewriting forks the codebase from upstream Tetra.
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface TetraItemPredicate extends Predicate<ItemStack> {
    boolean matches(ItemStack itemStack);

    @Override
    default boolean test(ItemStack itemStack) {
        return matches(itemStack);
    }
}
