package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.MergingDataStore;
import se.mickelus.tetra.module.schematic.SchematicDefinition;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class SchematicStore extends MergingDataStore<SchematicDefinition, SchematicDefinition[]> {

    public SchematicStore(Gson gson, String namespace, String directory, DataDistributor distributor) {
        super(gson, namespace, directory, SchematicDefinition.class, SchematicDefinition[].class, distributor);
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
    }
}
