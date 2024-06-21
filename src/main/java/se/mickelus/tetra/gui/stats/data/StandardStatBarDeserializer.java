package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class StandardStatBarDeserializer {
    public static GuiStatBase deserialize(JsonElement jsonElement) {
        StandardData data = StatBarStore.gson.fromJson(jsonElement, StandardData.class);
        return new GuiStatBar(0, 0, StatsHelper.barLength, data.key, data.min, data.max,
                data.segmented != null ? data.segmented : false, data.split != null ? data.split : false, data.inverted != null ? data.inverted : false,
                data.stat, data.label, data.tooltip)
                .setContexts(data.contexts != null ? data.contexts : new String[0])
                .setIndicators(data.indicators != null ? data.indicators : new GuiStatIndicator[0]);
    }

    record StandardData(String key, String[] contexts, double min, double max, Boolean segmented, Boolean split, Boolean inverted,
            IStatGetter stat, ILabelGetter label, ITooltipGetter tooltip, GuiStatIndicator[] indicators) {
    }
}
