package se.mickelus.tetra.compat.forge.items.wrapper;

import net.minecraft.world.Container;
import se.mickelus.tetra.compat.forge.items.IItemHandler;

public class InvWrapper extends net.neoforged.neoforge.items.wrapper.InvWrapper implements IItemHandler {
    public InvWrapper(Container inv) {
        super(inv);
    }
}
