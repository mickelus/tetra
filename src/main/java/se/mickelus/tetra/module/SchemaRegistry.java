package se.mickelus.tetra.module;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.schema.*;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static SchemaRegistry instance;

    private Map<ResourceLocation, UpgradeSchema> schemaMap;
    private Map<ResourceLocation, UpgradeSchema> dynamicSchemas;

    public SchemaRegistry() {
        instance = this;

        schemaMap = Collections.emptyMap();
        dynamicSchemas = new HashMap<>();

        DataManager.schemaData.onReload(() -> setupSchemas(DataManager.schemaData.getData()));
    }

    /**
     * Register a schema that's not config driven
     * @param schema
     */
    public void registerSchema(UpgradeSchema schema) {
        dynamicSchemas.put(new ResourceLocation(TetraMod.MOD_ID, schema.getKey()), schema);
    }

    private void setupSchemas(Map<ResourceLocation, SchemaDefinition> data) {
        schemaMap = data.entrySet().stream()
                .filter(entry -> validateSchemaDefinition(entry.getKey(), entry.getValue()))
                .flatMap(entry -> createSchemas(entry.getKey(), entry.getValue()).stream())
                .filter(entry -> entry.getRight() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dynamicSchemas.forEach((identifier, schema) -> schemaMap.put(identifier, schema));

        RepairRegistry.instance.injectFromSchemas(data.values());
    }

    private boolean validateSchemaDefinition(ResourceLocation identifier, SchemaDefinition definition) {
        if (definition == null) {
            logger.warn("Failed to create schema from schema definition '{}': Data is null (probably due to it failing to parse)",
                    identifier);
            return false;
        }

        if (definition.slots == null || definition.slots.length < 1) {
            logger.warn("Failed to create schema from schema definition '{}': Slots field is empty", identifier);
            return false;
        }

        return true;
    }

    // todo: hacky stuff to get multislot modules to work, there has to be another way
    private Collection<Pair<ResourceLocation, ConfigSchema>> createSchemas(ResourceLocation identifier, SchemaDefinition definition) {
        if (definition.slots.length == definition.keySuffixes.length) {
            ArrayList<Pair<ResourceLocation, ConfigSchema>> result = new ArrayList<>(definition.slots.length);
            for (int i = 0; i < definition.slots.length; i++) {
                try {
                    ResourceLocation suffixedIdentifier = new ResourceLocation(
                            identifier.getNamespace(), identifier.getPath() + definition.keySuffixes[i]);

                    result.add(new ImmutablePair<>(suffixedIdentifier,
                            new ConfigSchema(definition, definition.keySuffixes[i], definition.slots[i])));
                } catch (InvalidSchemaException e) {
                    e.printMessage();
                }
            }

            return result;
        } else {
            try {
                return Collections.singletonList(new ImmutablePair<>(identifier, new ConfigSchema(definition)));
            } catch (InvalidSchemaException e) {
                e.printMessage();
            }
        }

        return Collections.singletonList(new ImmutablePair<>(identifier, null));
    }

    public UpgradeSchema getSchema(ResourceLocation identifier) {
        return schemaMap.get(identifier);
    }

    public Collection<UpgradeSchema> getAllSchemas() {
        return schemaMap.values();
    }
}
