package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

public class AcceptsImprovementRequirement implements CraftingRequirement {
    String improvement;
    Integer level;

    @Override
    public boolean test(CraftingContext context) {
        if (context.targetMajorModule != null) {
            if (level != null && !context.targetMajorModule.acceptsImprovementLevel(improvement, level)) {
                return false;
            }
            return context.targetMajorModule.acceptsImprovement(improvement);
        }
        return false;
    }
}
