package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.MaterialImprovementData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ImprovementStore extends DataStore<ImprovementData[]> {

    private final MaterialStore materialStore;

    public ImprovementStore(Gson gson, String namespace, String directory, MaterialStore materialStore, DataDistributor distributor) {
        super(gson, namespace, directory, ImprovementData[].class, distributor);

        this.materialStore = materialStore;
    }

    @Override
    protected void processData() {
        dataMap = dataMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> processData(entry.getValue())));
    }

    private ImprovementData[] processData(ImprovementData[] data) {
        return Arrays.stream(data)
                .flatMap(improvement ->
                        improvement instanceof MaterialImprovementData
                                ? expandMaterialImprovement((MaterialImprovementData) improvement)
                                : Stream.of(improvement))
                .toArray(ImprovementData[]::new);
    }

    private Stream<ImprovementData> expandMaterialImprovement(MaterialImprovementData data) {
        return Arrays.stream(data.materials)
                .map(rl -> rl.getPath().endsWith("/")
                        ? materialStore.getDataIn(rl)
                        : Optional.ofNullable(materialStore.getData(rl)).map(Collections::singletonList).orElseGet(Collections::emptyList))
                .flatMap(Collection::stream)
                .map(data::combine);
    }
}
