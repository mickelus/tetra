package se.mickelus.tetra.data;

import com.google.gson.Gson;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.MaterialImprovementData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class ImprovementStore extends DataStore<ImprovementData[]> {

    public ImprovementStore(Gson gson, String directory) {
        super(gson, directory, ImprovementData[].class);
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
                        ? DataManager.materialData.getDataIn(rl)
                        : Optional.ofNullable(DataManager.materialData.getData(rl)).map(Collections::singletonList).orElseGet(Collections::emptyList))
                .flatMap(Collection::stream)
                .map(data::combine);
    }
}
