package se.mickelus.tetra.generation.processing;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlock;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerTile;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedContainerProcessor extends StructureProcessor {
    public ForgedContainerProcessor() {}

    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, BlockPos pos2, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof ForgedContainerBlock) {
            Random random;

            // this ensures that both blockstates for the container gets generated from the same seed, then there's no need to sync later
            if (blockInfo.state.getValue(ForgedContainerBlock.flippedProp)) {
                random = placementSettings.getRandom(blockInfo.pos.relative(blockInfo.state.getValue(ForgedContainerBlock.facingProp).getCounterClockWise()));
            } else {
                random = placementSettings.getRandom(blockInfo.pos);
            }

            CompoundNBT newCompound = blockInfo.nbt.copy();

            int[] lockIntegrity = new int[ForgedContainerTile.lockCount];
            for (int i = 0; i < lockIntegrity.length; i++) {
                lockIntegrity[i] = 1 + random.nextInt(ForgedContainerTile.lockIntegrityMax - 1);
            }
            ForgedContainerTile.writeLockData(newCompound, lockIntegrity);

            int lidIntegrity = 1 + random.nextInt(ForgedContainerTile.lidIntegrityMax - 1);
            ForgedContainerTile.writeLidData(newCompound, lidIntegrity);

            BlockState newState = ForgedContainerTile.getUpdatedBlockState(blockInfo.state, lockIntegrity, lidIntegrity);

            return new Template.BlockInfo(blockInfo.pos, newState, newCompound);
        }

        return blockInfo;
    }

    @Override
    protected IStructureProcessorType getType() {
        return ProcessorTypes.forgedContainer;
    }
}