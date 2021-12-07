package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.generation.FeatureParameters;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FeatureStore extends DataStore<FeatureParameters> {
    public FeatureStore(Gson gson, String namespace, String directory, DataDistributor distributor) {
        super(gson, namespace, directory, FeatureParameters.class, distributor);
    }

    @Override
    protected void processData() {
        getData().forEach((rl, params) -> params.location = rl);
    }
}
