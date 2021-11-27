package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.module.data.MaterialData;

public class MaterialStore extends MergingDataStore<MaterialData, MaterialData[]> {

    public MaterialStore(Gson gson, String directory) {
        super(gson, directory, MaterialData.class, MaterialData[].class);
    }

    @Override
    protected MaterialData mergeData(MaterialData[] data) {
        if (data.length > 0) {
            MaterialData result = data[0];

            for (int i = 1; i < data.length; i++) {
                if (data[i].replace) {
                    result = data[i];
                } else {
                    MaterialData.copyFields(data[i], result);
                }
            }
            return result;
        }
        return null;
    }
}
