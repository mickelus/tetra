package se.mickelus.tetra.data;

import com.google.gson.Gson;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class FeatureStore extends DataStore<FeatureParameters> {
    public FeatureStore(Gson gson, String directory) {
        super(gson, directory, FeatureParameters.class);
    }

    @Override
    protected void processData() {
        getData().forEach((rl, params) -> params.location = rl);
    }
}
