package se.mickelus.tetra.gui.stats;

import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.StatGetterMultiply;
import se.mickelus.tetra.gui.stats.getter.StatGetterSum;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatsHelper {
    public static final int barLength = 59;

    static IStatGetter sum(IStatGetter... statGetters) {
        return new StatGetterSum(statGetters);
    }

    static IStatGetter sum(double offset, IStatGetter... statGetters) {
        return new StatGetterSum(offset, statGetters);
    }

    static IStatGetter multiply(IStatGetter... statGetters) {
        return new StatGetterMultiply(statGetters);
    }

    static IStatGetter[] withStats(IStatGetter... statGetters) {
        return statGetters;
    }

    static StatFormat[] withFormat(StatFormat... statGetters) {
        return statGetters;
    }
}
