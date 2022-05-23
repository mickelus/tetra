package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.Optional;

public class AspectRequirement implements CraftingRequirement {
    ItemAspect aspect;
    IntegerPredicate level;

    public AspectRequirement(ItemAspect aspect, IntegerPredicate level) {
        this.aspect = aspect;
        this.level = level;
    }

    @Override
    public boolean test(CraftingContext context) {
        return Optional.ofNullable(context.targetModule)
                .map(module -> module.getAspects(context.targetStack))
                .filter(aspects -> aspects.contains(aspect))
                .map(aspects -> level == null || level.test(aspects.getLevel(aspect)))
                .orElse(false);
    }
}
