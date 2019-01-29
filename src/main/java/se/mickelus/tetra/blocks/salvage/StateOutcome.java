package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StateOutcome<T extends Comparable<T>, V extends T> implements InteractionOutcome {

    private IProperty<T> property;
    private V value;

    public StateOutcome(IProperty<T> property, V value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public boolean apply(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumHand hand, EnumFacing hitFace) {
        blockState.withProperty(property, value);

        return true;
    }
}
