package se.mickelus.tetra.items.modular.impl.dynamic;

public record ArchetypeDefinition(String id, boolean honeable, int honeBase, int honeIntegrityMultiplier, ArchetypeSlotDefinition[] slots) {
}

