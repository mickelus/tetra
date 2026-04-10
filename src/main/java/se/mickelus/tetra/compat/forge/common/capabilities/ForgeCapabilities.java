package se.mickelus.tetra.compat.forge.common.capabilities;

import se.mickelus.tetra.compat.forge.items.IItemHandler;

public final class ForgeCapabilities {
    public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");

    private ForgeCapabilities() {}
}
