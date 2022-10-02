package se.mickelus.tetra.gui.stats.getter;

import se.mickelus.tetra.effect.ItemEffect;

public class StatGetterFocus extends StatGetterSpread {
    public StatGetterFocus() {
        super(ItemEffect.focus);
    }

    @Override
    protected double wrapEfficiency(double eff) {
        return eff;
    }
}
