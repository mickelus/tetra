package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.craftingeffect.CraftingEffect;

public class CraftingEffectStore extends MergingDataStore<CraftingEffect, CraftingEffect[]> {

    public CraftingEffectStore(Gson gson, String directory) {
        super(gson, directory, CraftingEffect.class, CraftingEffect[].class);
    }

    @Override
    protected CraftingEffect mergeData(CraftingEffect[] data) {
        if (data.length > 0) {
            CraftingEffect result = data[0];

            for (int i = 1; i < data.length; i++) {
                if (data[i].replace) {
                    result = data[i];
                } else {
                    CraftingEffect.copyFields(data[i], result);
                }
            }
            return result;
        }
        return null;
    }
}
