package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class OutcomeStack {
    UpgradeSchematic schematic;
    OutcomePreview preview;

    public OutcomeStack(UpgradeSchematic schematic, OutcomePreview preview) {
        this.schematic = schematic;
        this.preview = preview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutcomeStack that = (OutcomeStack) o;
        return schematicEquals(that.schematic) && previewEquals(that.preview);
    }

    public boolean schematicEquals(UpgradeSchematic schematic) {
        return Objects.equals(this.schematic.getKey(), schematic.getKey());
    }

    public boolean previewEquals(OutcomePreview preview) {
        return Objects.equals(this.preview, preview);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schematic, preview);
    }
}
