package net.minecraftforge.common.capabilities;

import net.minecraftforge.items.IItemHandler;

public final class ForgeCapabilities {
    public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");

    private ForgeCapabilities() {}
}
