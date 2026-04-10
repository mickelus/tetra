package se.mickelus.tetra.items;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class ItemPredicateComposite implements TetraItemPredicate {
    TetraItemPredicate[] predicates;

    public ItemPredicateComposite(TetraItemPredicate[] predicates) {
        this.predicates = predicates;
    }

    @Override
    public boolean matches(ItemStack item) {
        return Arrays.stream(predicates).anyMatch(predicate -> predicate.matches(item));
    }
}
