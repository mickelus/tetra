package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import se.mickelus.tetra.gui.stats.getter.IStatFormat;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;
import se.mickelus.tetra.gui.stats.getter.TooltipGetterMultiValue;

public class TooltipGetterDeserializers {
    public static ITooltipGetter defaultGetter(JsonElement json) {
        StandardData data = StatRegistry.gson.fromJson(json, StandardData.class);
        return new TooltipGetterMultiValue(data.key,
                data.stats != null ? data.stats : new IStatGetter[0],
                data.formatters != null ? data.formatters : new IStatFormat[0]);
    }

    record StandardData(String key, IStatGetter[] stats, IStatFormat[] formatters) {
    }
}
