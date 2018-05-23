package se.mickelus.tetra.module.schema;

import se.mickelus.tetra.module.data.CapabilityData;

import java.util.HashMap;

public class OutcomeDefinition {
    public Material material = new Material();
    public int materialSlot = 0;
    public int experienceCost = 0;
    public CapabilityData requiredCapabilities = new CapabilityData();
    public String moduleKey;
    public String moduleVariant;
    public HashMap<String, Integer> improvements = new HashMap<>();
}
