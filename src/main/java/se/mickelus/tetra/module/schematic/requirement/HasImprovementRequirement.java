package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

public class HasImprovementRequirement implements CraftingRequirement {
    String improvement;
    IntegerPredicate level;

    @Override
    public boolean test(CraftingContext context) {
        if (context.targetMajorModule != null) {
            if (level != null && !level.test(context.targetMajorModule.getImprovementLevel(context.targetStack, improvement))) {
                return false;
            }
            return context.targetMajorModule.getImprovement(context.targetStack, improvement) != null;
        }
        return false;
    }
}
