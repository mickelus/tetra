package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.module.data.ModuleData;

public class ModuleStore extends MergingDataStore<ModuleData, ModuleData[]> {

    public ModuleStore(Gson gson, String directory) {
        super(gson, directory, ModuleData.class, ModuleData[].class);
    }

    @Override
    protected ModuleData mergeData(ModuleData[] data) {
        if (data.length > 0) {
            ModuleData result = data[0];

            for (int i = 1; i < data.length; i++) {
                if (data[i].replace) {
                    result = data[i];
                } else {
                    ModuleData.copyFields(data[i], result);
                }
            }
            return result;
        }
        return null;
    }
}
