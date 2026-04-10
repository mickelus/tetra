package net.minecraftforge.items.wrapper;

import net.minecraft.world.Container;
import net.minecraftforge.items.IItemHandler;

public class InvWrapper extends net.neoforged.neoforge.items.wrapper.InvWrapper implements IItemHandler {
    public InvWrapper(Container inv) {
        super(inv);
    }
}
