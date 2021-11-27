package se.mickelus.tetra.module;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.schematic.SchematicDefinition;
import se.mickelus.tetra.module.schematic.RepairDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class RepairRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static RepairRegistry instance;

    private Map<String, List<RepairDefinition>> repairMap;
    private List<RepairDefinition> injectedRepairs;

    public RepairRegistry() {
        instance = this;

        repairMap = new HashMap<>();
        injectedRepairs = new LinkedList<>();

        DataManager.repairData.onReload(() -> setupDefinitions(DataManager.repairData.getData()));
    }

    public void injectFromSchematics(Collection<SchematicDefinition> schematicDefinitions) {
        injectedRepairs = schematicDefinitions.stream()
                .filter(schematicDefinition -> schematicDefinition.repair)
                .flatMap(schematicDefinition -> Arrays.stream(schematicDefinition.outcomes))
                .filter(RepairDefinition::validateOutcome)
                .map(RepairDefinition::new)
                .collect(Collectors.toList());
    }

    private void setupDefinitions(Map<ResourceLocation, RepairDefinition> data) {
        repairMap.clear();

        injectedRepairs.forEach(this::putDefinition);

        data.entrySet().stream()
                .filter(entry -> validate(entry.getKey(), entry.getValue()))
                .forEach(entry -> putDefinition(entry.getValue()));
    }

    private void putDefinition(RepairDefinition definition) {
        if (definition.replace && repairMap.containsKey(definition.moduleVariant)) {
            repairMap.get(definition.moduleVariant).clear();
        }

        repairMap.computeIfAbsent(definition.moduleVariant, key -> new ArrayList<>())
                .add(definition);
    }

    private boolean validate(ResourceLocation identifier, RepairDefinition definition) {
        if (definition == null) {
            logger.warn("Failed to load repair definition '{}': Data is null (probably due to it failing to parse)",
                    identifier);
            return false;
        }

        if (definition.material == null) {
            logger.warn("Failed to load repair definition '{}': material field is empty", identifier);
            return false;
        }

        if (definition.moduleKey == null) {
            logger.warn("Failed to load repair definition '{}': moduleKey field is empty", identifier);
            return false;
        }

        if (definition.moduleVariant == null) {
            logger.warn("Failed to load repair definition '{}': moduleVariant field is empty", identifier);
            return false;
        }

        return true;
    }

    public List<RepairDefinition> getDefinitions(String moduleVariant) {
        return repairMap.getOrDefault(moduleVariant, Collections.emptyList());
    }
}
