package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.TooltipGetterMultiValue;

public class TooltipGetterDeserializers {
    public static ITooltipGetter defaultGetter(JsonElement json) {
        StandardData data = StatBarStore.gson.fromJson(json, StandardData.class);
        return new TooltipGetterMultiValue(data.key, data.stats, data.formatters);
    }

    record StandardData(String key, IStatGetter[] stats, StatFormat[] formatters) {
    }
}
