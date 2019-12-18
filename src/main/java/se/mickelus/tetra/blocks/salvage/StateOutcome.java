package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StateOutcome<T extends Comparable<T>, V extends T> implements InteractionOutcome {

    private Property<T> property;
    private V value;

    public StateOutcome(Property<T> property, V value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public boolean apply(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        blockState.with(property, value);

        return true;
    }
}
