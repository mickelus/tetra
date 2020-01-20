package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.module.schema.SchemaDefinition;

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
    }
}
