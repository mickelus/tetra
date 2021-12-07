package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.MergingDataStore;
import se.mickelus.tetra.craftingeffect.CraftingEffect;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CraftingEffectStore extends MergingDataStore<CraftingEffect, CraftingEffect[]> {

    public CraftingEffectStore(Gson gson, String namespace, String directory, DataDistributor distributor) {
        super(gson, namespace, directory, CraftingEffect.class, CraftingEffect[].class, distributor);
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
