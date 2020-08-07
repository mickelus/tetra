package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.module.schematic.OutcomeDefinition;
import se.mickelus.tetra.module.schematic.SchematicDefinition;

import java.util.*;

public class SchematicStore extends MergingDataStore<SchematicDefinition, SchematicDefinition[]> {

    public SchematicStore(Gson gson, String directory) {
        super(gson, directory, SchematicDefinition.class, SchematicDefinition[].class);
    }

    @Override
    protected SchematicDefinition mergeData(SchematicDefinition[] collection) {
        if (collection.length > 0) {
            SchematicDefinition result = collection[0];

            for (int i = 1; i < collection.length; i++) {
                if (collection[i].replace) {
                    result = collection[i];
                } else {
                    SchematicDefinition.copyFields(collection[i], result);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    protected void processData() {
        dataMap.forEach((key, value) -> value.key = key.getPath());

        // put all tag outcomes at the top, so that they are matched last
        for (SchematicDefinition schematic : dataMap.values()) {
            schematic.outcomes = Arrays.stream(schematic.outcomes)
                    .sorted((a, b) -> Boolean.compare(b.material != null && b.material.isTagged(), a.material != null && a.material.isTagged()))
                    .toArray(OutcomeDefinition[]::new);
        }
    }
}
