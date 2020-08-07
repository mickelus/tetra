package se.mickelus.tetra.module;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.schematic.*;

import java.util.*;
import java.util.stream.Collectors;

public class SchematicRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static SchematicRegistry instance;

    private Map<ResourceLocation, UpgradeSchematic> schematicMap;
    private Map<ResourceLocation, UpgradeSchematic> dynamicSchematics;

    public SchematicRegistry() {
        instance = this;

        schematicMap = Collections.emptyMap();
        dynamicSchematics = new HashMap<>();

        DataManager.schematicData.onReload(() -> setupSchematics(DataManager.schematicData.getData()));
    }

    /**
     * Register a schematic that's not config driven
     * @param schematic
     */
    public void registerSchematic(UpgradeSchematic schematic) {
        dynamicSchematics.put(new ResourceLocation(TetraMod.MOD_ID, schematic.getKey()), schematic);
    }

    private void setupSchematics(Map<ResourceLocation, SchematicDefinition> data) {
        schematicMap = data.entrySet().stream()
                .filter(entry -> validateSchematicDefinition(entry.getKey(), entry.getValue()))
                .flatMap(entry -> createSchematics(entry.getKey(), entry.getValue()).stream())
                .filter(entry -> entry.getRight() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dynamicSchematics.forEach((identifier, schematic) -> schematicMap.put(identifier, schematic));

        RepairRegistry.instance.injectFromSchematics(data.values());
    }

    private boolean validateSchematicDefinition(ResourceLocation identifier, SchematicDefinition definition) {
        if (definition == null) {
            logger.warn("Failed to create schematic from schematic definition '{}': Data is null (probably due to it failing to parse)",
                    identifier);
            return false;
        }

        if (definition.slots == null || definition.slots.length < 1) {
            logger.warn("Failed to create schematic from schematic definition '{}': Slots field is empty", identifier);
            return false;
        }

        return true;
    }

    // todo: hacky stuff to get multislot modules to work, there has to be another way
    private Collection<Pair<ResourceLocation, ConfigSchematic>> createSchematics(ResourceLocation identifier, SchematicDefinition definition) {
        if (definition.slots.length == definition.keySuffixes.length) {
            ArrayList<Pair<ResourceLocation, ConfigSchematic>> result = new ArrayList<>(definition.slots.length);
            for (int i = 0; i < definition.slots.length; i++) {
                try {
                    ResourceLocation suffixedIdentifier = new ResourceLocation(
                            identifier.getNamespace(), identifier.getPath() + definition.keySuffixes[i]);

                    result.add(new ImmutablePair<>(suffixedIdentifier,
                            new ConfigSchematic(definition, definition.keySuffixes[i], definition.slots[i])));
                } catch (InvalidSchematicException e) {
                    e.printMessage();
                }
            }

            return result;
        } else {
            try {
                return Collections.singletonList(new ImmutablePair<>(identifier, new ConfigSchematic(definition)));
            } catch (InvalidSchematicException e) {
                e.printMessage();
            }
        }

        return Collections.singletonList(new ImmutablePair<>(identifier, null));
    }

    public UpgradeSchematic getSchematic(ResourceLocation identifier) {
        return schematicMap.get(identifier);
    }

    public Collection<UpgradeSchematic> getAllSchematics() {
        return schematicMap.values();
    }
}
