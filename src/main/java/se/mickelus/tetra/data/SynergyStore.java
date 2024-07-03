package se.mickelus.tetra.data;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.data.SynergyData;

import java.util.Arrays;

public class SynergyStore extends DataStore<SynergyData[]> {
    public SynergyStore(Gson gson, String namespace, String directory,
            DataDistributor synchronizer) {
        super(gson, namespace, directory, SynergyData[].class, synchronizer);
    }

    /**
     * Wrapped data getter for synergy data so that data may be ordered in such a way that it's efficiently compared. Skipping this step
     * would cause items to incorrectly gain synergies.
     *
     * @param path The path to the synergy data
     * @return An array of synergy data
     */
    public SynergyData[] getOrdered(String path) {
        SynergyData[] data = getDataIn(new ResourceLocation(TetraMod.MOD_ID, path)).stream()
                .flatMap(Arrays::stream)
                .toArray(SynergyData[]::new);
        for (SynergyData entry : data) {
            Arrays.sort(entry.moduleVariants);
            Arrays.sort(entry.modules);
        }
        return data;
    }
}
