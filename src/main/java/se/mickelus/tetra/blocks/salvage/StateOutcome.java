package se.mickelus.tetra.blocks.salvage;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class StateOutcome<T extends Comparable<T>, V extends T> implements InteractionOutcome {

    private Property<T> property;
    private V value;

    public StateOutcome(Property<T> property, V value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public boolean apply(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction hitFace) {
        blockState.setValue(property, value);

        return true;
    }
}
