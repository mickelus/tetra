package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.util.Mth;
import se.mickelus.tetra.util.StringHelper;

public class StatFormatRoman implements IStatFormat {
    public static final StatFormatRoman instance = new StatFormatRoman();

    @Override
    public String get(double value) {
        int roundedValue = Mth.clamp((int) Math.round(value), 1, 3999);
        return StringHelper.toRoman(roundedValue);
    }
}
