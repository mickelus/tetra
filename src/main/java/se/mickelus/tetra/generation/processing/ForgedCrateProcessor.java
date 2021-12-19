package se.mickelus.tetra.generation.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import se.mickelus.tetra.blocks.forged.ForgedCrateBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class ForgedCrateProcessor extends StructureProcessor {
    public ForgedCrateProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $, StructureTemplate.StructureBlockInfo blockInfo,
            StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state.getBlock() instanceof ForgedCrateBlock) {
            Random random = placementSettings.getRandom(blockInfo.pos);

            BlockState blockState = blockInfo.state
                    .setValue(ForgedCrateBlock.propIntegrity, random.nextInt(4))
                    .setValue(ForgedCrateBlock.propFacing, Direction.from2DDataValue(random.nextInt(4)));

            return new StructureTemplate.StructureBlockInfo(blockInfo.pos, blockState, blockInfo.nbt);
        }

        return blockInfo;
    }

    @Override
    protected StructureProcessorType getType() {
        return ProcessorTypes.forgedCrate;
    }
}