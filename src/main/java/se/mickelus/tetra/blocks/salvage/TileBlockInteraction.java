package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.util.TileEntityOptional;

import java.util.function.Function;

public class TileBlockInteraction<T extends TileEntity> extends BlockInteraction {

    private final Function<T, Boolean> predicate;
    private final Class<T> tileEntityClass;

    public TileBlockInteraction(ToolType requiredTool, int requiredLevel, Direction face, float minX, float maxX, float minY, float maxY,
            Class<T> tileEntityClass, Function<T, Boolean> predicate, InteractionOutcome outcome) {
        super(requiredTool, requiredLevel, face, minX, maxX, minY, maxY, outcome);

        this.tileEntityClass = tileEntityClass;
        this.predicate = predicate;
    }

    @Override
    public boolean applicableForBlock(World world, BlockPos pos, BlockState blockState) {
        return TileEntityOptional.from(world, pos, tileEntityClass)
                .map(predicate::apply)
                .orElse(false);
    }
}
