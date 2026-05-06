package se.mickelus.tetra.blocks;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface ItemHandlerBlockEntity {
    IItemHandler getItemHandler(@Nullable Direction side);
}
