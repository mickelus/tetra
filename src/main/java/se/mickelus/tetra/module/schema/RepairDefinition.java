package se.mickelus.tetra.module.schema;

import se.mickelus.tetra.module.data.CapabilityData;


public class RepairDefinition {
    public Material material = new Material();
    public CapabilityData requiredCapabilities = new CapabilityData();
    public String moduleKey;
    public String moduleVariant;

    public RepairDefinition(OutcomeDefinition outcomeDefinition) {
        moduleKey = outcomeDefinition.moduleKey;
        moduleVariant = outcomeDefinition.moduleVariant;

        material = outcomeDefinition.material;
        requiredCapabilities = outcomeDefinition.requiredCapabilities;
    }
}
