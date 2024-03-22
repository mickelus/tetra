package se.mickelus.tetra.module.schematic;

import se.mickelus.tetra.module.ItemModule;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public record RepairInstance(Collection<RepairDefinition> definitions, @Nullable ItemModule module) {
}
