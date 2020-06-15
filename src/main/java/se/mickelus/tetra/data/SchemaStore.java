package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.module.schema.OutcomeDefinition;
import se.mickelus.tetra.module.schema.SchemaDefinition;

import java.util.*;

public class SchemaStore extends MergingDataStore<SchemaDefinition, SchemaDefinition[]> {

    public SchemaStore(Gson gson, String directory) {
        super(gson, directory, SchemaDefinition.class, SchemaDefinition[].class);
    }

    @Override
    protected SchemaDefinition mergeData(SchemaDefinition[] collection) {
        if (collection.length > 0) {
            SchemaDefinition result = collection[0];

            for (int i = 1; i < collection.length; i++) {
                if (collection[i].replace) {
                    result = collection[i];
                } else {
                    SchemaDefinition.copyFields(collection[i], result);
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
        for (SchemaDefinition schema : dataMap.values()) {
            schema.outcomes = Arrays.stream(schema.outcomes)
                    .sorted((a, b) -> Boolean.compare(b.material != null && b.material.isTagged(), a.material != null && a.material.isTagged()))
                    .toArray(OutcomeDefinition[]::new);
        }
    }
}
