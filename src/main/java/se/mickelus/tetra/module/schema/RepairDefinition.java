package se.mickelus.tetra.module.schema;

import se.mickelus.tetra.module.data.CapabilityData;


public class RepairDefinition {
    public Material material;
    public CapabilityData requiredCapabilities;
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
        requiredCapabilities = outcomeDefinition.requiredCapabilities;
    }

    public static boolean validateOutcome(OutcomeDefinition outcome) {
        return outcome.moduleVariant != null && outcome.material != null && outcome.material.predicate != null;
    }
}
