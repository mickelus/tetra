package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import se.mickelus.tetra.gui.stats.getter.IStatFormat;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.sorting.BasicStatSorter;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;

public class BasicStatSorterDeserializer {
    public static IStatSorter deserialize(JsonElement jsonElement) {
        StandardData data = StatRegistry.gson.fromJson(jsonElement, StandardData.class);
        BasicStatSorter result = new BasicStatSorter(data.stat, data.key, data.format)
                .setSuffix(data.suffix);
        if (data.inverted != null && data.inverted) {
            result.setInverted();
        }
        return result;
    }

    record StandardData(String key, IStatGetter stat, IStatFormat format, Boolean inverted, String suffix) {
    }
}
