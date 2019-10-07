package se.mickelus.tetra.items;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class ItemPredicateComposite extends ItemPredicate {
    ItemPredicate[] predicates;

    public ItemPredicateComposite(ItemPredicate[] predicates) {
        this.predicates = predicates;
    }

    @Override
    public boolean test(ItemStack item) {
        return Arrays.stream(predicates).anyMatch(predicate -> predicate.test(item));
    }
}
