package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import se.mickelus.tetra.capabilities.Capability;

import java.util.Collection;

public interface IBlockCapabilityInteractive {
    public BlockInteraction[] getPotentialInteractions(BlockState blockState, Direction face, Collection<Capability> capabilities);
}
