package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class IndicatorDeserializers {
    public static GuiStatIndicator standardGetter(JsonElement json) {
        StandardData data = StatRegistry.gson.fromJson(json, StandardData.class);
        return new GuiStatIndicator(0, 0, data.key, data.textureX, data.textureY, data.texture, data.stat, data.tooltip);
    }

    record StandardData(String key, int textureX, int textureY, ResourceLocation texture, IStatGetter stat, ITooltipGetter tooltip) {
    }
}
