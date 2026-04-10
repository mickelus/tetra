package se.mickelus.tetra.compat.forge.common.capabilities;

public final class Capability<T> {
    private final String name;

    public Capability(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
