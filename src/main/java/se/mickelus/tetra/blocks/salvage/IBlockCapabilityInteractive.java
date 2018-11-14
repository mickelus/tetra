package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import se.mickelus.tetra.capabilities.Capability;

import java.util.Collection;

public interface IBlockCapabilityInteractive {
    public BlockInteraction[] getPotentialInteractions(IBlockState blockState, EnumFacing face, Collection<Capability> capabilities);
}
