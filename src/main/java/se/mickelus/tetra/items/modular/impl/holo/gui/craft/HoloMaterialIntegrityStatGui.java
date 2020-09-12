package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.tetra.gui.statbar.getter.LabelGetterBasic;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.List;
import java.util.function.Function;

public class HoloMaterialIntegrityStatGui extends HoloMaterialStatGui {

    public HoloMaterialIntegrityStatGui(int x, int y) {
        super(x, y, "integrity", LabelGetterBasic.integerLabel, data -> data.integrityGain);

    }

    public void update(MaterialData current, MaterialData preview) {
        String gain = valueFormatter.getLabelMerged(current.integrityGain, preview.integrityGain);
        String cost = valueFormatter.getLabelMerged(current.integrityCost, preview.integrityCost);

        value.setString(gain + " " + cost);
    }

    @Override
    public List<String> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }
}
