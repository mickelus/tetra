package se.mickelus.tetra.items;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class ItemPredicateComposite extends ItemPredicate {
    ItemPredicate[] predicates;

    public ItemPredicateComposite(ItemPredicate[] predicates) {
        this.predicates = predicates;
    }

    @Override
    public boolean matches(ItemStack item) {
        return Arrays.stream(predicates).anyMatch(predicate -> predicate.matches(item));
    }
}
