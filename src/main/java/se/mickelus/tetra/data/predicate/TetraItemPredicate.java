package se.mickelus.tetra.data.predicate;

import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@FunctionalInterface
public interface TetraItemPredicate extends Predicate<ItemStack> {
    boolean matches(ItemStack itemStack);

    @Override
    default boolean test(ItemStack itemStack) {
        return matches(itemStack);
    }
}
