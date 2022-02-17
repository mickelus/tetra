package se.mickelus.tetra.gui.stats;

import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.StatGetterMultiply;
import se.mickelus.tetra.gui.stats.getter.StatGetterSum;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatsHelper {
    public static final int barLength = 59;

    public static IStatGetter sum(IStatGetter... statGetters) {
        return new StatGetterSum(statGetters);
    }

    public static IStatGetter sum(double offset, IStatGetter... statGetters) {
        return new StatGetterSum(offset, statGetters);
    }

    public static IStatGetter multiply(IStatGetter... statGetters) {
        return new StatGetterMultiply(statGetters);
    }

    public static IStatGetter multiply(double multiplier, IStatGetter... statGetters) {
        return new StatGetterMultiply(multiplier, statGetters);
    }

    public static IStatGetter[] withStats(IStatGetter... statGetters) {
        return statGetters;
    }

    public static StatFormat[] withFormat(StatFormat... statGetters) {
        return statGetters;
    }
}
