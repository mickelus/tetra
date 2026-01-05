package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import net.minecraft.world.entity.ai.attributes.Attributes;
import se.mickelus.tetra.gui.stats.getter.*;

public class TooltipGetterDeserializers {
    public static ITooltipGetter defaultGetter(JsonElement json) {
        StandardData data = StatRegistry.gson.fromJson(json, StandardData.class);
        return new TooltipGetterMultiValue(data.key,
                data.stats != null ? data.stats : new IStatGetter[0],
                data.formatters != null ? data.formatters : new IStatFormat[0]);
    }

    record StandardData(String key, IStatGetter[] stats, IStatFormat[] formatters) {
    }

    public static ITooltipGetter counterweight(JsonElement json) {
        return new TooltipGetterCounterweight();
    }

    public static ITooltipGetter attackSpeed(JsonElement json) {
        return new TooltipGetterAttackSpeed(new StatGetterAttribute(Attributes.ATTACK_SPEED));
    }
}
