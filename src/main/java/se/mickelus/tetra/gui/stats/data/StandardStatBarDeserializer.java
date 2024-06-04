package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class StandardStatBarDeserializer {
    public static GuiStatBase deserialize(JsonElement jsonElement) {
        StandardData data = DataManager.gson.fromJson(jsonElement, StandardData.class);
        return new GuiStatBar(0, 0, StatsHelper.barLength, data.label, data.min, data.max,
                data.segmented != null ? data.segmented : false, data.split != null ? data.split : false, data.inverted != null ? data.inverted : false,
                data.statGetter, data.labelGetter, data.tooltipGetter);
    }

    record StandardData(String label, double min, double max, Boolean segmented, Boolean split, Boolean inverted,
                        IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
    }
}
