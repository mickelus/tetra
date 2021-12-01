package se.mickelus.tetra.module.schematic;

import se.mickelus.tetra.module.data.ToolData;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class RepairDefinition {
    public OutcomeMaterial material;
    public ToolData requiredTools;
    public String moduleKey;
    public String moduleVariant;

    /**
     * If set to true, all previous (lower load order) repair definitions for the same module variant will be removed
     */
    public boolean replace = false;

    public RepairDefinition(OutcomeDefinition outcomeDefinition) {
        moduleKey = outcomeDefinition.moduleKey;
        moduleVariant = outcomeDefinition.moduleVariant;

        material = outcomeDefinition.material;
        requiredTools = outcomeDefinition.requiredTools;
    }

    public static boolean validateOutcome(OutcomeDefinition outcome) {
        return outcome.moduleVariant != null && outcome.material != null && outcome.material.isValid();
    }
}
