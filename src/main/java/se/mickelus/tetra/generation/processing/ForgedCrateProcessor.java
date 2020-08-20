package se.mickelus.tetra.generation.processing;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.blocks.forged.ForgedCrateBlock;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedCrateProcessor extends StructureProcessor {
    public ForgedCrateProcessor() {}

    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, BlockPos pos2, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof ForgedCrateBlock) {
            Random random = placementSettings.getRandom(blockInfo.pos);

            BlockState blockState = blockInfo.state
                    .with(ForgedCrateBlock.propIntegrity, random.nextInt(4))
                    .with(ForgedCrateBlock.propFacing, Direction.byHorizontalIndex(random.nextInt(4)));

            return new Template.BlockInfo(blockInfo.pos, blockState, blockInfo.nbt);
        }

        return blockInfo;
    }

    @Override
    protected IStructureProcessorType getType() {
        return ProcessorTypes.forgedCrate;
    }
}