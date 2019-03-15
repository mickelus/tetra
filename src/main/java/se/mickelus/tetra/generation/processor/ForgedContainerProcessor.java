package se.mickelus.tetra.generation.processor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;
import se.mickelus.tetra.blocks.forged.container.BlockForgedContainer;
import se.mickelus.tetra.blocks.forged.container.TileEntityForgedContainer;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedContainerProcessor implements ITemplateProcessor {

    private Random random;

    public ForgedContainerProcessor(Random random) {
        this.random = random;
    }

    @Nullable
    @Override
    public Template.BlockInfo processBlock(World world, BlockPos pos, Template.BlockInfo blockInfo) {
        System.out.println(blockInfo.blockState.getBlock());
        if (blockInfo.blockState.getBlock() instanceof BlockForgedContainer) {
            int[] lockIntegrity = new int[TileEntityForgedContainer.lockCount];
            for (int i = 0; i < lockIntegrity.length; i++) {
                lockIntegrity[i] = 1 + random.nextInt(TileEntityForgedContainer.lockIntegrityMax - 1);
            }

            TileEntityForgedContainer.writeLockData(blockInfo.tileentityData, lockIntegrity);
            TileEntityForgedContainer.writeLidData(blockInfo.tileentityData, 1 + random.nextInt(TileEntityForgedContainer.lidIntegrityMax - 1));
        }
        return blockInfo;
    }
}